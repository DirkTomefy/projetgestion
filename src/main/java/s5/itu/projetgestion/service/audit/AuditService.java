package s5.itu.projetgestion.service.audit;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.model.audit.JournalAudit;
import s5.itu.projetgestion.repository.audit.JournalAuditRepository;

/** Journal d'audit : qui a fait quoi, quand, et avec quel résultat. */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    /** Libellés lisibles des actions (méthode + motif d'URL), du plus précis au plus général. */
    private static final List<String[]> LIBELLES = List.of(
            new String[]{"POST", "/auth/login", "Connexion"},
            new String[]{"POST", "/auth/logout", "Déconnexion"},
            new String[]{"POST", "/auth/inscription", "Inscription"},
            new String[]{"POST", "/auth/mot-de-passe", "Changement de mot de passe"},
            new String[]{"PUT", "/auth/profil", "Modification du profil"},
            new String[]{"POST", "/api/admin/utilisateurs", "Création d'un compte"},
            new String[]{"PUT", "/api/admin/utilisateurs/\\d+", "Modification d'un compte"},
            new String[]{"DELETE", "/api/admin/utilisateurs/\\d+", "Suppression d'une inscription"},
            new String[]{"POST", "/api/admin/utilisateurs/\\d+/activer", "Activation d'un compte"},
            new String[]{"POST", "/api/admin/utilisateurs/\\d+/desactiver", "Désactivation d'un compte"},
            new String[]{"POST", "/api/admin/utilisateurs/\\d+/reinitialiser", "Réinitialisation d'un mot de passe"},
            new String[]{"POST", "/api/rh/besoins", "Expression d'un besoin"},
            new String[]{"POST", "/api/rh/motifs", "Création d'un motif"},
            new String[]{"POST", "/api/besoins/\\d+/annuler", "Annulation d'un besoin"},
            new String[]{"POST", "/api/besoin/valider", "Validation d'un besoin"},
            new String[]{"POST", "/api/besoin/refuser", "Refus d'un besoin"},
            new String[]{"POST", "/api/rh/postes/demande", "Demande de création de poste"},
            new String[]{"PUT", "/api/postes/\\d+/etat", "Décision sur un poste proposé"},
            new String[]{"POST", "/api/contrats/demandes", "Demande de nouveau contrat ou d'avenant"},
            new String[]{"POST", "/api/contrats/demandes/\\d+/instruire", "Instruction d'une demande de contrat"},
            new String[]{"POST", "/api/contrats/demandes/\\d+/finance", "Avis de la Finance sur une demande de contrat"},
            new String[]{"POST", "/api/contrats/demandes/\\d+/decider", "Décision du DG sur une demande de contrat"},
            new String[]{"POST", "/api/contrats/demandes/\\d+/annuler", "Annulation d'une demande de contrat"},
            new String[]{"POST", "/api/organisation/postes", "Création d'un poste"},
            new String[]{"PUT", "/api/organisation/postes/\\d+", "Modification d'un poste"},
            new String[]{"DELETE", "/api/organisation/postes/\\d+", "Suppression d'un poste"},
            new String[]{"*", "/api/organisation/unites.*", "Gestion des unités"},
            new String[]{"*", "/api/organisation/circuit-validation.*", "Circuit de validation des besoins"},
            new String[]{"*", "/api/referentiels/.*", "Gestion des référentiels"},
            new String[]{"POST", "/api/finance/grilles", "Nouvelle grille salariale"},
            new String[]{"POST", "/api/finance/grilles/\\d+/valider", "Validation d'une grille salariale"},
            new String[]{"POST", "/api/finance/grilles/\\d+/refuser", "Refus d'une grille salariale"},
            new String[]{"POST", "/api/finance/budgets/\\d+/valider", "Validation d'un budget"},
            new String[]{"POST", "/api/finance/budgets/\\d+/refuser", "Refus d'un budget"},
            new String[]{"*", "/api/finance/budgets.*", "Gestion des budgets"},
            new String[]{"POST", "/api/rh/offres", "Création d'une offre d'emploi"},
            new String[]{"POST", "/api/rh/offres/\\d+/publier", "Publication d'une offre d'emploi"},
            new String[]{"POST", "/api/rh/offres/\\d+/cloturer", "Clôture d'une offre d'emploi"},
            new String[]{"POST", "/api/public/candidatures", "Dépôt d'une candidature"},
            new String[]{"POST", "/api/rh/candidatures/\\d+/preselection", "Présélection d'une candidature"},
            new String[]{"POST", "/api/rh/candidatures/\\d+/refus", "Refus d'une candidature"},
            new String[]{"*", "/api/rh/(offres/\\d+/)?niveaux-selection.*", "Niveaux de sélection"},
            new String[]{"PUT", "/api/rh/evaluations/\\d+/planification", "Planification d'une évaluation"},
            new String[]{"POST", "/api/selection/evaluations/\\d+/resultat", "Résultat d'une évaluation"},
            new String[]{"POST", "/api/rh/propositions", "Proposition d'embauche"},
            new String[]{"POST", "/api/rh/propositions/\\d+/contre-proposition", "Contre-proposition"},
            new String[]{"POST", "/api/rh/propositions/\\d+/retrait", "Retrait d'une proposition"},
            new String[]{"POST", "/api/public/propositions/[^/]+/reponse", "Réponse d'un candidat à une proposition"},
            new String[]{"POST", "/api/public/propositions/[^/]+/declin", "Déclin d'une proposition par le candidat"},
            new String[]{"POST", "/api/embauches", "Embauche"},
            new String[]{"POST", "/api/rh/contrats/\\d+/valider", "Validation d'un contrat"},
            new String[]{"POST", "/api/rh/contrats/\\d+/signer", "Signature d'un contrat"},
            new String[]{"POST", "/api/rh/contrats/\\d+/annuler", "Annulation d'un contrat"},
            new String[]{"POST", "/api/rh/contrats/\\d+/renouveler", "Renouvellement d'un contrat"},
            new String[]{"POST", "/api/rh/contrats/\\d+/document", "Dépôt du contrat signé"},
            new String[]{"POST", "/api/rh/employes", "Saisie d'un employé"},
            new String[]{"PUT", "/api/rh/employes/\\d+", "Modification d'un employé"},
            new String[]{"POST", "/api/rh/employes/\\d+/mutation", "Mutation d'un employé"},
            new String[]{"POST", "/api/rh/employes/\\d+/depart", "Départ d'un employé"},
            new String[]{"POST", "/api/rh/employes/\\d+/banque", "Coordonnées bancaires"},
            new String[]{"POST", "/api/onboarding/taches/\\d+", "Tâche d'onboarding"},
            new String[]{"*", "/api/onboarding/modeles.*", "Modèle d'onboarding"},
            new String[]{"POST", "/api/delegations?", "Délégation de validation"},
            new String[]{"POST", "/api/delegations?/\\d+/annuler", "Fin d'une délégation"}
    );

    /** Actions sans intérêt pour l'audit (lecture des notifications, aperçus). */
    private static final List<String> IGNORES = List.of("/api/notifications.*", "/api/rh/offres/apercu");

    private final JournalAuditRepository repo;

    public AuditService(JournalAuditRepository repo) {
        this.repo = repo;
    }

    /** Enregistré dans sa propre transaction : l'échec d'une action reste tracé. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void tracer(Integer idUser, String login, String methode, String chemin, int statut, String ip) {
        if (IGNORES.stream().anyMatch(chemin::matches)) return;
        try {
            JournalAudit j = new JournalAudit();
            j.setIdUser(idUser);
            j.setLogin(login);
            j.setMethode(methode);
            j.setChemin(chemin.length() > 300 ? chemin.substring(0, 300) : chemin);
            j.setStatutHttp(statut);
            j.setAdresseIp(ip);
            j.setAction(libelle(methode, chemin));
            repo.save(j);
        } catch (RuntimeException e) {
            log.warn("Journal d'audit non enregistré pour {} {} : {}", methode, chemin, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<JournalAudit> rechercher(String q, LocalDate du, LocalDate au, String resultat, int limite) {
        String texte = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        int succes = "SUCCES".equalsIgnoreCase(resultat) ? 1 : "ECHEC".equalsIgnoreCase(resultat) ? 2 : 0;
        return repo.rechercher(texte, du == null ? LocalDate.of(1970, 1, 1).atStartOfDay() : du.atStartOfDay(),
                au == null ? LocalDate.of(3000, 1, 1).atStartOfDay() : au.plusDays(1).atStartOfDay(),
                succes, PageRequest.of(0, Math.max(1, Math.min(limite, 1000))));
    }

    static String libelle(String methode, String chemin) {
        for (String[] l : LIBELLES) {
            if (("*".equals(l[0]) || l[0].equals(methode)) && Pattern.matches(l[1], chemin)) {
                return l[2] + ("*".equals(l[0]) ? " (" + verbe(methode) + ")" : "");
            }
        }
        return verbe(methode) + " " + chemin;
    }

    private static String verbe(String methode) {
        return switch (methode) {
            case "POST" -> "création / action";
            case "PUT", "PATCH" -> "modification";
            case "DELETE" -> "suppression";
            default -> methode;
        };
    }
}
