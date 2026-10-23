package s5.itu.projetgestion.service.recrutement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.err.ProjetgestionException;
import s5.itu.projetgestion.model.recrutement.OffreEmploi;
import s5.itu.projetgestion.repository.recrutement.OffreEmploiRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Clôture des offres (bloc 2) :
 *  - automatique, date limite de candidature dépassée (tâche quotidienne) ;
 *  - automatique, tous les sièges du besoin POURVU (après une embauche, et chaque jour) ;
 *  - manuelle, bouton « Clôturer » de la RH.
 * Une offre clôturée n'est plus visible sur le site public et n'accepte plus de candidature,
 * mais reste consultable par la RH avec ses candidatures (la sélection et les propositions continuent).
 */
@Service
public class OffreClotureService {

    private static final Logger log = LoggerFactory.getLogger(OffreClotureService.class);

    private final OffreEmploiRepository offreRepo;

    public OffreClotureService(OffreEmploiRepository offreRepo) {
        this.offreRepo = offreRepo;
    }

    /** Bouton « Clôturer » (RH). */
    @Transactional
    public OffreEmploi cloturer(Integer idOffre, SessionUser user) {
        if (user == null || !user.hasRole(OffreEmploiService.ROLE_RH)) {
            throw new AccesRefuseException("Action réservée au rôle RH");
        }
        OffreEmploi o = offreRepo.findById(idOffre)
                .orElseThrow(() -> new ProjetgestionException("Offre introuvable : " + idOffre));
        if (OffreEmploi.ETAT_CLOTUREE.equals(o.getEtat())) {
            throw new ProjetgestionException("L'offre " + o.getRef() + " est déjà clôturée");
        }
        appliquer(o, OffreEmploi.CLOTURE_MANUELLE, user.getId());
        return o;
    }

    /** Offres publiées dont la date limite est passée (strictement avant aujourd'hui). */
    @Transactional
    public int cloturerDatesDepassees(LocalDate aujourdhui) {
        List<OffreEmploi> offres = offreRepo.findByEtatAndDateLimiteCandidatureBefore(
                OffreEmploi.ETAT_PUBLIEE, aujourdhui);
        offres.forEach(o -> appliquer(o, OffreEmploi.CLOTURE_DATE_LIMITE, null));
        return offres.size();
    }

    /**
     * Offres dont tous les sièges sont pourvus. idBesoin NULL : tous les besoins.
     * Appelée dans la transaction de l'acceptation d'une proposition, après l'embauche.
     */
    @Transactional
    public int cloturerSiPostesPourvus(Integer idBesoin) {
        List<OffreEmploi> offres = offreRepo.findACloturerPostesPourvus(idBesoin);
        offres.forEach(o -> appliquer(o, OffreEmploi.CLOTURE_POSTES_POURVUS, null));
        return offres.size();
    }

    private void appliquer(OffreEmploi o, String motif, Integer idUser) {
        o.setEtat(OffreEmploi.ETAT_CLOTUREE);
        o.setDateCloture(LocalDateTime.now());
        o.setMotifCloture(motif);
        o.setIdUserCloture(idUser);
        offreRepo.save(o);
        log.info("Offre {} clôturée ({})", o.getRef(), motif);
    }
}
