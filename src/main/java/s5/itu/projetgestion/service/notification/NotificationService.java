package s5.itu.projetgestion.service.notification;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.dto.notification.NotificationDto;
import s5.itu.projetgestion.err.PropositionException;
import s5.itu.projetgestion.model.notification.Notification;
import s5.itu.projetgestion.repository.auth.UserRepository;
import s5.itu.projetgestion.repository.notification.NotificationRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Service commun de notification dans l'application (P4), appelé par chaque membre aux moments clés.
 * Il s'exécute dans la transaction de l'appelant : la notification n'existe que si l'action est enregistrée.
 *
 * <pre>
 * // P3 : une demande attend votre validation (étape = poste valideur de workflow_validation)
 * notificationService.notifierPoste(idPosteValideur, NotificationService.VALIDATION_EN_ATTENTE,
 *         "Le besoin " + ref + " attend votre validation", "/validations/" + idBesoin);
 * // P5 : une nouvelle candidature est arrivée
 * notificationService.notifierRole("RH", NotificationService.CANDIDATURE_RECUE,
 *         "Nouvelle candidature de " + nom + " pour l'offre " + refOffre, "/rh/offres/" + idOffre + "/candidatures");
 * // P2 : un contrat arrive à échéance (une seule fois par contrat : notifierUneFois)
 * notificationService.notifierRoleUneFois("RH", NotificationService.CONTRAT_ECHEANCE, message, "/contrats/" + idContrat);
 * // P1 : une tâche d'onboarding vous est assignée (poste responsable de la tâche)
 * notificationService.notifierPoste(idPosteResponsable, NotificationService.ONBOARDING_TACHE, message, "/onboarding/" + id);
 * </pre>
 */
@Service
@Transactional
public class NotificationService {

    public static final String VALIDATION_EN_ATTENTE = "VALIDATION_EN_ATTENTE";
    public static final String CANDIDATURE_RECUE = "CANDIDATURE_RECUE";
    public static final String PROPOSITION_REPONSE = "PROPOSITION_REPONSE";
    public static final String PROPOSITION_EXPIREE = "PROPOSITION_EXPIREE";
    public static final String CONTRAT_ECHEANCE = "CONTRAT_ECHEANCE";
    public static final String ONBOARDING_TACHE = "ONBOARDING_TACHE";

    private static final int MESSAGE_MAX = 2000;
    private static final int LIEN_MAX = 500;
    private static final int LIMITE_MAX = 100;

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;

    public NotificationService(NotificationRepository notificationRepo, UserRepository userRepo) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
    }

    // ------------------------------------------------------------------ envoi

    /** Une notification pour un utilisateur. */
    public Notification notifier(Integer idUtilisateur, String type, String message, String lien) {
        if (idUtilisateur == null) throw new IllegalArgumentException("Destinataire de notification manquant");
        if (type == null || type.isBlank()) throw new IllegalArgumentException("Type de notification manquant");
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Message de notification manquant");
        Notification n = new Notification();
        n.setUtilisateur(userRepo.getReferenceById(idUtilisateur));
        n.setType(type.trim());
        n.setMessage(couper(message.trim(), MESSAGE_MAX));
        n.setLien(lien == null || lien.isBlank() ? null : couper(lien.trim(), LIEN_MAX));
        n.setLu(false);
        return notificationRepo.save(n);
    }

    /** Une notification pour chaque utilisateur. Retourne le nombre de notifications créées. */
    public int notifier(Iterable<Integer> idsUtilisateurs, String type, String message, String lien) {
        Set<Integer> ids = new LinkedHashSet<>();
        idsUtilisateurs.forEach(ids::add);
        ids.forEach(id -> notifier(id, type, message, lien));
        return ids.size();
    }

    /** Tous les utilisateurs d'un rôle (ex. "RH"). */
    public int notifierRole(String role, String type, String message, String lien) {
        return notifier(notificationRepo.findIdsUtilisateursParRole(role), type, message, lien);
    }

    /**
     * Les occupants actuels d'un poste (affectation en cours), et leurs délégués pendant une délégation en cours
     * (message suffixé « par délégation de … »).
     */
    public int notifierPoste(Integer idPoste, String type, String message, String lien) {
        List<Integer> titulaires = notificationRepo.findIdsUtilisateursParPoste(idPoste);
        int nb = notifier(titulaires, type, message, lien);
        if (!titulaires.isEmpty()) {
            for (NotificationRepository.DelegationRow d : notificationRepo.findDelegationsEnCours(titulaires)) {
                if (titulaires.contains(d.getIdDelegue())) continue;
                notifier(d.getIdDelegue(), type, message + " (par délégation de " + d.getDelegant() + ")", lien);
                nb++;
            }
        }
        return nb;
    }

    /** Comme notifierRole, sans doublon : un utilisateur déjà notifié pour ce type et ce lien ne l'est pas deux fois. */
    public int notifierRoleUneFois(String role, String type, String message, String lien) {
        List<Integer> ids = new ArrayList<>();
        for (Integer id : notificationRepo.findIdsUtilisateursParRole(role)) {
            if (!notificationRepo.existsByUtilisateur_IdAndTypeAndLien(id, type, lien)) ids.add(id);
        }
        return notifier(ids, type, message, lien);
    }

    // ------------------------------------------------------------------ lecture (cloche)

    @Transactional(readOnly = true)
    public List<NotificationDto> lister(Integer idUtilisateur, boolean nonLuesSeulement, int limite) {
        int taille = Math.max(1, Math.min(limite, LIMITE_MAX));
        return notificationRepo.findPourUtilisateur(idUtilisateur, nonLuesSeulement, PageRequest.of(0, taille))
                .stream().map(NotificationService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public long compterNonLues(Integer idUtilisateur) {
        return notificationRepo.countByUtilisateur_IdAndLuFalse(idUtilisateur);
    }

    /** Seul le destinataire peut marquer sa notification comme lue. */
    public NotificationDto marquerLue(Integer idNotification, Integer idUtilisateur) {
        Notification n = notificationRepo.findByIdAndUtilisateur_Id(idNotification, idUtilisateur)
                .orElseThrow(() -> PropositionException.introuvable("Notification introuvable : " + idNotification));
        n.setLu(true);
        return toDto(n);
    }

    public int marquerToutLu(Integer idUtilisateur) {
        return notificationRepo.marquerToutLu(idUtilisateur);
    }

    private static NotificationDto toDto(Notification n) {
        return new NotificationDto(n.getId(), n.getType(), n.getMessage(), n.getLien(), n.isLu(), n.getDateCreation());
    }

    private static String couper(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
