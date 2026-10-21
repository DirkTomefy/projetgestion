package s5.itu.projetgestion.service.postes;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import s5.itu.projetgestion.dto.postes.PosteDto;
import s5.itu.projetgestion.model.postes.Poste;
import s5.itu.projetgestion.repository.postes.PosteRepository;

@Service
public class PosteService {

    private static final Set<String> ETATS = Set.of("PROPOSE", "ACTIF", "REFUSE", "SUPPRIME");

    private final PosteRepository posteRepository;

    public PosteService(PosteRepository posteRepository) {
        this.posteRepository = posteRepository;
    }

    @Transactional(readOnly = true)
    public List<PosteDto> listerParEtat(String etat) {
        if (!ETATS.contains(etat)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "État inconnu : " + etat);
        }
        return posteRepository.findByEtatAvecRelations(etat).stream()
                .map(PosteDto::from)
                .toList();
    }

    /**
     * Traite un poste PROPOSE :
     * - ACTIF  : validation
     * - REFUSE : refus (SUPPRIME est réservé à la suppression d'un poste déjà actif)
     */
    @Transactional
    public PosteDto traiterProposition(Integer id, String nouvelEtat) {
        if (!"ACTIF".equals(nouvelEtat) && !"REFUSE".equals(nouvelEtat)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Seuls les états ACTIF (validation) et REFUSE (refus) sont autorisés.");
        }

        Poste poste = posteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poste introuvable : " + id));

        if (!"PROPOSE".equals(poste.getEtat())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le poste n'est plus à l'état PROPOSE (état actuel : " + poste.getEtat() + ").");
        }

        poste.setEtat(nouvelEtat);
        return PosteDto.from(posteRepository.save(poste));
    }
}