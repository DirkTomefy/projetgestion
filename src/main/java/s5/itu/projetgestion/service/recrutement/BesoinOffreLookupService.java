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

    public Optional<BesoinOffreInfo> findById(Integer id) {
        if (id == null) return Optional.empty();
        return repo.findInfo(id).map(BesoinOffreLookupService::toInfo);
    }

    public List<BesoinOffreInfo> findRecrutementOuvert() {
        return repo.findRecrutementOuvert().stream().map(BesoinOffreLookupService::toInfo).toList();
    }

    private static BesoinOffreInfo toInfo(BesoinOffreRepository.BesoinOffreRow r) {
        return new BesoinOffreInfo(r.getId(), r.getReference(), r.getTypeBesoin(), r.getEffectif(), r.getDateSouhaitee(),
                r.getEtatActuel(), r.getPrecisionMotif(), r.getMotif(), r.getUnite(), r.getUniteType(), r.getPoste(),
                r.getMetier(), r.getCategorie(), r.getEchelon(), r.getMontantMin(), r.getMontantMax());
    }
}
