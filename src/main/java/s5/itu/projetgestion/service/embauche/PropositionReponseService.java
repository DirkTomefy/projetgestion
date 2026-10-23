package s5.itu.projetgestion.service.embauche;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.dto.embauche.MotifRequest;
import s5.itu.projetgestion.dto.embauche.PropositionPubliqueDto;
import s5.itu.projetgestion.dto.embauche.ReponseCandidatRequest;
import s5.itu.projetgestion.err.PropositionException;
import s5.itu.projetgestion.model.besoin.BesoinPoste;
import s5.itu.projetgestion.model.embauche.PropositionCondition;
import s5.itu.projetgestion.model.embauche.PropositionEmbauche;
import s5.itu.projetgestion.model.embauche.RelanceCondition;
import s5.itu.projetgestion.model.recrutement.Candidat;
import s5.itu.projetgestion.model.recrutement.OffreEmploi;
import s5.itu.projetgestion.repository.embauche.PropositionEmbaucheRepository;
import s5.itu.projetgestion.repository.embauche.RelanceConditionRepository;
import s5.itu.projetgestion.repository.recrutement.OffreEmploiRepository;
import s5.itu.projetgestion.service.notification.CommunicationCandidatService;
import s5.itu.projetgestion.service.notification.NotificationService;
import s5.itu.projetgestion.service.recrutement.OffreClotureService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Réponse du candidat depuis la page publique (décision 5 : lien personnel avec un jeton, sans compte).
 *  - il accepte ou refuse chaque condition en attente ;
 *  - un refus partiel attend une contre-proposition de la RH (relance_condition) ;
 *  - tout accepté : statut ACCEPTER, puis embauche automatique (P3) et clôture de l'offre si tous ses
 *    sièges sont pourvus, dans la même transaction : si l'embauche échoue, l'acceptation est annulée ;
 *  - passé la date limite sans réponse : EXPIREE.
 */
@Service
@Transactional
public class PropositionReponseService {

    private static final Logger log = LoggerFactory.getLogger(PropositionReponseService.class);

    private final PropositionEmbaucheRepository propositionRepo;
    private final RelanceConditionRepository relanceRepo;
    private final OffreEmploiRepository offreRepo;
    private final PropositionMapper mapper;
    private final HistoPropositionService histoService;
    private final PropositionExpirationService expirationService;
    private final OffreClotureService clotureService;
    private final ObjectProvider<EmbaucheService> embaucheService;
    private final NotificationService notificationService;
    private final CommunicationCandidatService communication;

    public PropositionReponseService(PropositionEmbaucheRepository propositionRepo,
                                     RelanceConditionRepository relanceRepo,
                                     OffreEmploiRepository offreRepo,
                                     PropositionMapper mapper,
                                     HistoPropositionService histoService,
                                     PropositionExpirationService expirationService,
                                     OffreClotureService clotureService,
                                     ObjectProvider<EmbaucheService> embaucheService,
                                     NotificationService notificationService,
                                     CommunicationCandidatService communication) {
        this.propositionRepo = propositionRepo;
        this.relanceRepo = relanceRepo;
        this.offreRepo = offreRepo;
        this.mapper = mapper;
        this.histoService = histoService;
        this.expirationService = expirationService;
        this.clotureService = clotureService;
        this.embaucheService = embaucheService;
        this.notificationService = notificationService;
        this.communication = communication;
    }

    /** Page publique : la proposition telle que le candidat la voit (expire à la volée si besoin). */
    public PropositionPubliqueDto consulter(String jeton) {
        PropositionEmbauche p = parJeton(jeton);
        expirationService.expirerSiNecessaire(p);
        return toDto(p);
    }

    public PropositionPubliqueDto repondre(String jeton, ReponseCandidatRequest req) {
        PropositionEmbauche p = parJetonVerrouille(jeton);
        verifierAttenteCandidat(p);

        List<PropositionCondition> conditions = mapper.conditionsTriees(p.getId());
        Map<Integer, PropositionCondition> enAttente = conditions.stream()
                .filter(PropositionCondition::enAttente)
                .collect(Collectors.toMap(PropositionCondition::getId, c -> c));
        Map<Integer, ReponseCandidatRequest.ReponseCondition> reponses = new HashMap<>();
        for (ReponseCandidatRequest.ReponseCondition r : req.reponses()) {
            if (!enAttente.containsKey(r.idCondition())) {
                throw PropositionException.invalide("reponses", "Cette condition n'attend pas de réponse : " + r.idCondition());
            }
            if (reponses.put(r.idCondition(), r) != null) {
                throw PropositionException.invalide("reponses", "Une condition a reçu deux réponses");
            }
        }
        if (!reponses.keySet().equals(enAttente.keySet())) {
            throw PropositionException.invalide("reponses", "Répondez à chaque condition avant d'envoyer");
        }

        List<String> refus = new ArrayList<>();
        for (PropositionCondition c : conditions) {
            ReponseCandidatRequest.ReponseCondition r = reponses.get(c.getId());
            if (r == null) continue;
            boolean accepte = r.accepte();
            c.setAccepte(accepte);
            String motif = accepte ? null : blankToNull(r.commentaire());
            c.setCommentaireCandidat(motif);
            relanceRepo.findFirstByPropositionCondition_IdAndStatutOrderByIdDesc(c.getId(), RelanceCondition.STATUT_EN_ATTENTE)
                    .ifPresent(rel -> rel.setStatut(accepte ? RelanceCondition.STATUT_ACCEPTEE : RelanceCondition.STATUT_REFUSEE));
            if (!accepte) {
                refus.add(ConditionsProposition.libelle(c.getCode()) + (motif == null ? "" : " (« " + motif + " »)"));
            }
        }
        p.setDateReponse(LocalDateTime.now());

        if (refus.isEmpty() && conditions.stream().allMatch(PropositionCondition::estAcceptee)) {
            accepter(p);
        } else {
            histoService.enregistrer(p, null, "Le candidat refuse : " + String.join(" ; ", refus)
                    + " — en attente d'une contre-proposition de la RH");
            propositionRepo.save(p);
            notifierRh(p, "a refusé : " + String.join(" ; ", refus) + ". Une contre-proposition est attendue.");
        }
        return toDto(p);
    }

    /** Le candidat décline toute la proposition. */
    public PropositionPubliqueDto decliner(String jeton, MotifRequest req) {
        PropositionEmbauche p = parJetonVerrouille(jeton);
        if (expirationService.expirerSiNecessaire(p)) {
            throw PropositionException.conflit("Le délai de réponse est dépassé");
        }
        if (!p.estEnCours()) {
            throw PropositionException.conflit("Cette proposition n'attend plus de réponse");
        }
        String motif = req == null ? null : blankToNull(req.commentaire());
        p.setDateReponse(LocalDateTime.now());
        histoService.changerStatut(p, PropositionEmbauche.STATUT_REFUSER, null,
                "Le candidat décline la proposition" + (motif == null ? "" : " : " + motif));
        propositionRepo.save(p);
        notifierRh(p, "a décliné la proposition" + (motif == null ? "." : " : " + motif));
        return toDto(p);
    }

    private void accepter(PropositionEmbauche p) {
        histoService.changerStatut(p, PropositionEmbauche.STATUT_ACCEPTER, null, "Le candidat accepte toutes les conditions");
        propositionRepo.saveAndFlush(p);

        EmbaucheService embauche = embaucheService.getIfAvailable();
        if (embauche == null) {
            log.warn("Proposition {} acceptée mais aucun EmbaucheService (P3) n'est disponible : embauche non lancée",
                    p.getId());
            notifierRh(p, "a accepté toutes les conditions (embauche automatique non disponible).");
            return;
        }
        Integer idContrat = embauche.embaucher(p.getId());
        log.info("Proposition {} acceptée : contrat {} créé", p.getId(), idContrat);
        notifierRh(p, "a accepté toutes les conditions : employé et contrat créés.");
        communication.embaucheConfirmee(p);

        propositionRepo.flush();   // sièges POURVU écrits par l'embauche, avant la requête de clôture
        BesoinPoste siege = p.getBesoinPoste();
        clotureService.cloturerSiPostesPourvus(siege.getBesoin().getId());
    }

    /** P4 : « un candidat a répondu à une proposition » → tous les utilisateurs RH. */
    private void notifierRh(PropositionEmbauche p, String quoi) {
        Candidat ca = p.getCandidature().getCandidat();
        notificationService.notifierRole("RH", NotificationService.PROPOSITION_REPONSE,
                ca.getNom() + (ca.getPrenom() == null ? "" : " " + ca.getPrenom()) + " (" + p.getBesoinPoste().getPoste().getLibelle()
                        + ") " + quoi, "/rh/propositions/" + p.getId());
    }

    private void verifierAttenteCandidat(PropositionEmbauche p) {
        if (expirationService.expirerSiNecessaire(p)) {
            throw PropositionException.conflit("Le délai de réponse est dépassé : cette proposition a expiré");
        }
        List<PropositionCondition> conditions = mapper.conditionsTriees(p.getId());
        if (!PropositionMapper.ETAPE_ATTENTE_CANDIDAT.equals(PropositionMapper.etape(p.getStatutActuel(), conditions))) {
            throw PropositionException.conflit("Cette proposition n'attend pas de réponse de votre part");
        }
    }

    private PropositionEmbauche parJeton(String jeton) {
        if (jeton == null || jeton.isBlank() || jeton.length() > 64) throw introuvable();
        return propositionRepo.findByJeton(jeton).orElseThrow(PropositionReponseService::introuvable);
    }

    private PropositionEmbauche parJetonVerrouille(String jeton) {
        if (jeton == null || jeton.isBlank() || jeton.length() > 64) throw introuvable();
        return propositionRepo.findByJetonPourReponse(jeton).orElseThrow(PropositionReponseService::introuvable);
    }

    private static PropositionException introuvable() {
        return PropositionException.introuvable("Lien de proposition invalide ou inconnu");
    }

    private PropositionPubliqueDto toDto(PropositionEmbauche p) {
        List<PropositionCondition> conditions = mapper.conditionsTriees(p.getId());
        String etape = PropositionMapper.etape(p.getStatutActuel(), conditions);
        Candidat ca = p.getCandidature().getCandidat();
        BesoinPoste bp = p.getBesoinPoste();
        OffreEmploi offre = offreRepo.findById(p.getCandidature().getIdOffreEmploi()).orElse(null);
        String message = switch (etape) {
            case PropositionMapper.ETAPE_ATTENTE_CANDIDAT -> "Merci d'accepter ou de refuser chaque condition avant le "
                    + ConditionsProposition.date(p.getDateLimiteReponse()) + ".";
            case PropositionMapper.ETAPE_ATTENTE_RH -> "Votre réponse a bien été reçue. Le service RH étudie vos remarques "
                    + "et reviendra vers vous avec une nouvelle proposition.";
            case PropositionMapper.ETAPE_ACCEPTEE -> "Vous avez accepté la proposition. Bienvenue ! "
                    + "Le service RH vous contactera pour la signature du contrat.";
            case PropositionMapper.ETAPE_EXPIREE -> "Le délai de réponse est dépassé : cette proposition a expiré.";
            default -> "Cette proposition est close.";
        };
        return new PropositionPubliqueDto(etape, message, PropositionMapper.ETAPE_ATTENTE_CANDIDAT.equals(etape),
                ca.getNom(), ca.getPrenom(), bp.getPoste().getLibelle(),
                bp.getPoste().getUniteOrga() != null ? bp.getPoste().getUniteOrga().getLibelle() : null,
                offre != null ? offre.getRef() : null, p.getDateLimiteReponse(),
                mapper.conditionsDto(conditions, mapper.relances(p.getId())));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
