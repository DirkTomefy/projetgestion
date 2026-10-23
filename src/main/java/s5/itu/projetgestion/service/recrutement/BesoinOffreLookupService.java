package s5.itu.projetgestion.service.recrutement;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.dto.recrutement.BesoinOffreInfo;
import s5.itu.projetgestion.repository.besoin.BesoinOffreRepository;

/** Informations d'un besoin utiles à l'offre d'emploi (requêtes dans BesoinOffreRepository). */
@Service
@Transactional(readOnly = true)
public class BesoinOffreLookupService {

    public static final List<String> ETATS_RECRUTEMENT = List.of("RECRUTEMENT_OUVERT", "OUVERT");

    private final BesoinOffreRepository repo;

    public BesoinOffreLookupService(BesoinOffreRepository repo) {
        this.repo = repo;
    }

    /**
     * Informations du besoin pour un de ses postes. idPoste NULL : le premier poste du besoin
     * (offre antérieure couvrant tout le besoin, ou besoin d'un seul poste).
     */
    public Optional<BesoinOffreInfo> findById(Integer id, Integer idPoste) {
        if (id == null) return Optional.empty();
        List<BesoinOffreInfo> postes = postes(id);
        if (idPoste == null) return postes.stream().findFirst();
        return postes.stream().filter(p -> idPoste.equals(p.idPoste())).findFirst();
    }

    /** Postes demandés par le besoin, une ligne par poste. */
    public List<BesoinOffreInfo> postes(Integer idBesoin) {
        return repo.findPostes(idBesoin).stream().map(BesoinOffreLookupService::toInfo).toList();
    }

    public List<BesoinOffreInfo> findRecrutementOuvert() {
        return repo.findRecrutementOuvert().stream().map(BesoinOffreLookupService::toInfo).toList();
    }

    private static BesoinOffreInfo toInfo(BesoinOffreRepository.BesoinOffreRow r) {
        return new BesoinOffreInfo(r.getId(), r.getReference(), r.getTypeBesoin(), r.getEffectif(), r.getDateSouhaitee(),
                r.getEtatActuel(), r.getPrecisionMotif(), r.getMotif(), r.getUnite(), r.getUniteType(), r.getPoste(),
                r.getMetier(), r.getCategorie(), r.getEchelon(), r.getMontantMin(), r.getMontantMax(),
                r.getIdPoste(), r.getAPourvoir());
    }
}
