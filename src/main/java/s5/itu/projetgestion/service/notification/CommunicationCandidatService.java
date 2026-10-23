package s5.itu.projetgestion.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import s5.itu.projetgestion.dto.embauche.ConditionDto;
import s5.itu.projetgestion.dto.recrutement.BesoinOffreInfo;
import s5.itu.projetgestion.model.embauche.PointCondition;
import s5.itu.projetgestion.model.embauche.PropositionEmbauche;
import s5.itu.projetgestion.model.recrutement.Candidat;
import s5.itu.projetgestion.model.recrutement.Candidature;
import s5.itu.projetgestion.model.recrutement.OffreEmploi;
import s5.itu.projetgestion.repository.recrutement.OffreEmploiRepository;
import s5.itu.projetgestion.service.embauche.ConditionsProposition;
import s5.itu.projetgestion.service.embauche.PropositionMapper;
import s5.itu.projetgestion.service.recrutement.BesoinOffreLookupService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Emails aux candidats (P4, sprint 3) :
 *  - accusé de réception de la candidature ;
 *  - lien de réponse à la proposition (et à chaque contre-proposition) ;
 *  - résultat : refus (candidature REFUSE, ou proposition retirée par la RH) ou embauche.
 * Chaque méthode est sans risque pour l'appelant : toute erreur est journalisée, jamais propagée.
 */
@Service
public class CommunicationCandidatService {

    private static final Logger log = LoggerFactory.getLogger(CommunicationCandidatService.class);

    private final EmailService emailService;
    private final OffreEmploiRepository offreRepo;
    private final BesoinOffreLookupService besoinLookup;
    private final PropositionMapper mapper;
    private final String frontUrl;

    public CommunicationCandidatService(EmailService emailService, OffreEmploiRepository offreRepo,
                                        BesoinOffreLookupService besoinLookup, PropositionMapper mapper,
                                        @Value("${app.front.url:http://localhost:5173}") String frontUrl) {
        this.emailService = emailService;
        this.offreRepo = offreRepo;
        this.besoinLookup = besoinLookup;
        this.mapper = mapper;
        this.frontUrl = frontUrl.endsWith("/") ? frontUrl.substring(0, frontUrl.length() - 1) : frontUrl;
    }

    public void candidatureRecue(Candidature c) {
        securise("accusé de réception", () -> {
            Map<String, Object> v = base(c.getCandidat());
            ajouterOffre(v, c.getIdOffreEmploi());
            v.put("dateDepot", ConditionsProposition.date(c.getDateDepot() != null ? c.getDateDepot() : LocalDate.now()));
            emailService.envoyer(c.getCandidat().getEmail(),
                    "Candidature reçue" + suffixeOffre(v), "emails/candidature-recue", v);
        });
    }

    public void candidatureRefusee(Candidature c) {
        securise("refus", () -> {
            Map<String, Object> v = base(c.getCandidat());
            ajouterOffre(v, c.getIdOffreEmploi());
            emailService.envoyer(c.getCandidat().getEmail(),
                    "Votre candidature" + suffixeOffre(v), "emails/candidature-refusee", v);
        });
    }

    /** Convocation à un test ou entretien (ou nouvelle date en cas de replanification). */
    public void evaluationPlanifiee(Candidature c, String typeEvaluation, LocalDate date, String libelle, boolean replanification) {
        securise("convocation", () -> {
            Map<String, Object> v = base(c.getCandidat());
            ajouterOffre(v, c.getIdOffreEmploi());
            v.put("typeEvaluation", typeEvaluation);
            v.put("date", date == null ? null : ConditionsProposition.date(date));
            v.put("libelle", libelle);
            v.put("replanification", replanification);
            emailService.envoyer(c.getCandidat().getEmail(),
                    (replanification ? "Nouvelle date : " : "Convocation : ") + typeEvaluation + suffixeOffre(v),
                    "emails/evaluation-planifiee", v);
        });
    }

    public void propositionEnvoyee(PropositionEmbauche p, boolean contreProposition) {
        securise("proposition", () -> {
            Map<String, Object> v = proposition(p);
            v.put("contreProposition", contreProposition);
            List<ConditionDto> conditions = mapper.conditionsDto(mapper.conditionsTriees(p.getId()), mapper.relances(p.getId()));
            v.put("conditions", conditions);
            v.put("lien", frontUrl + "/proposition/" + p.getJeton());
            v.put("dateLimite", ConditionsProposition.date(p.getDateLimiteReponse()));
            emailService.envoyer(p.getCandidature().getCandidat().getEmail(),
                    (contreProposition ? "Nouvelle proposition d'embauche – " : "Proposition d'embauche – ") + v.get("poste"),
                    "emails/proposition", v);
        });
    }

    public void propositionRetiree(PropositionEmbauche p) {
        securise("proposition retirée", () -> {
            Map<String, Object> v = proposition(p);
            emailService.envoyer(p.getCandidature().getCandidat().getEmail(),
                    "Votre candidature – " + v.get("poste"), "emails/candidature-refusee", v);
        });
    }

    public void embaucheConfirmee(PropositionEmbauche p) {
        securise("embauche", () -> {
            Map<String, Object> v = proposition(p);
            mapper.conditionsTriees(p.getId()).forEach(c -> {
                if (PointCondition.DATE_DEBUT.equals(c.getCode())) v.put("dateDebut", mapper.afficher(c.getCode(), c.getValeur()));
                if (PointCondition.TYPE_CONTRAT.equals(c.getCode())) v.put("typeContrat", mapper.afficher(c.getCode(), c.getValeur()));
                if (PointCondition.LIEU.equals(c.getCode())) v.put("lieu", c.getValeur());
            });
            emailService.envoyer(p.getCandidature().getCandidat().getEmail(),
                    "Bienvenue ! Votre embauche – " + v.get("poste"), "emails/embauche", v);
        });
    }

    // ------------------------------------------------------------------

    private Map<String, Object> base(Candidat ca) {
        Map<String, Object> v = new HashMap<>();
        v.put("nom", ca.getNom());
        v.put("prenom", ca.getPrenom());
        return v;
    }

    private Map<String, Object> proposition(PropositionEmbauche p) {
        Map<String, Object> v = base(p.getCandidature().getCandidat());
        v.put("poste", p.getBesoinPoste().getPoste().getLibelle());
        v.put("unite", p.getBesoinPoste().getPoste().getUniteOrga() != null
                ? p.getBesoinPoste().getPoste().getUniteOrga().getLibelle() : null);
        ajouterOffre(v, p.getCandidature().getIdOffreEmploi());
        v.put("poste", p.getBesoinPoste().getPoste().getLibelle());   // le siège proposé prime sur le poste de l'offre
        return v;
    }

    private void ajouterOffre(Map<String, Object> v, Integer idOffre) {
        OffreEmploi o = idOffre == null ? null : offreRepo.findById(idOffre).orElse(null);
        if (o == null) return;
        v.put("referenceOffre", o.getRef());
        BesoinOffreInfo b = besoinLookup.findById(o.getIdBesoin(), o.getIdPoste()).orElse(null);
        if (b != null) {
            v.putIfAbsent("poste", b.poste());
            v.putIfAbsent("unite", b.unite());
        }
    }

    private static String suffixeOffre(Map<String, Object> v) {
        Object poste = v.get("poste");
        return poste == null ? "" : " – " + poste;
    }

    private static void securise(String quoi, Runnable r) {
        try {
            r.run();
        } catch (Exception e) {
            log.warn("Email « {} » non préparé : {}", quoi, e.getMessage(), e);
        }
    }
}
