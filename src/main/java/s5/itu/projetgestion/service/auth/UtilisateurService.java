package s5.itu.projetgestion.service.auth;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import s5.itu.projetgestion.dto.auth.InscriptionRequest;
import s5.itu.projetgestion.dto.auth.MotDePasseRequest;
import s5.itu.projetgestion.dto.auth.UtilisateurDto;
import s5.itu.projetgestion.dto.auth.UtilisateurRequest;
import s5.itu.projetgestion.dto.commun.OptionDto;
import s5.itu.projetgestion.model.auth.Role;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.model.organisation.Affectation;
import s5.itu.projetgestion.model.organisation.Employe;
import s5.itu.projetgestion.repository.auth.RoleRepository;
import s5.itu.projetgestion.repository.auth.UserRepository;
import s5.itu.projetgestion.repository.organisation.AffectationRepository;
import s5.itu.projetgestion.repository.organisation.EmployeRepository;
import s5.itu.projetgestion.service.notification.NotificationService;

/** Comptes utilisateurs : inscription, administration, mot de passe. */
@Service
@Transactional
public class UtilisateurService {

    public static final Set<String> STATUTS = Set.of("ACTIF", "EN_ATTENTE", "DESACTIVE");
    private static final String LOGIN_REGEX = "[a-zA-Z0-9._-]{3,50}";
    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    private static final SecureRandom ALEA = new SecureRandom();

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final EmployeRepository employeRepo;
    private final AffectationRepository affectationRepo;
    private final PasswordService passwordService;
    private final NotificationService notificationService;

    public UtilisateurService(UserRepository userRepo, RoleRepository roleRepo, EmployeRepository employeRepo,
                              AffectationRepository affectationRepo, PasswordService passwordService,
                              NotificationService notificationService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.employeRepo = employeRepo;
        this.affectationRepo = affectationRepo;
        this.passwordService = passwordService;
        this.notificationService = notificationService;
    }

    // ------------------------------------------------------------------ inscription (public)

    public UtilisateurDto inscrire(InscriptionRequest req) {
        if (req == null) throw new IllegalArgumentException("Formulaire d'inscription manquant.");
        String login = controlerLogin(req.login());
        String nom = obligatoire(req.nomComplet(), "Le nom complet est obligatoire.");
        String email = controlerEmail(req.email(), true);
        if (req.motDePasse() == null || !req.motDePasse().equals(req.confirmation()))
            throw new IllegalArgumentException("Les deux mots de passe ne correspondent pas.");
        passwordService.controlerRobustesse(req.motDePasse());

        User u = new User();
        u.setLogin(login);
        u.setNomComplet(nom);
        u.setEmail(email);
        u.setMotDePasse(passwordService.hacher(req.motDePasse()));
        u.setStatut("EN_ATTENTE");
        if (req.matricule() != null && !req.matricule().isBlank()) {
            // rattachement proposé ; l'administrateur le confirme en validant le compte
            employeRepo.findByMatriculeIgnoreCase(req.matricule().trim()).ifPresent(e -> {
                if (!userRepo.existsByIdEmployeAndIdNot(e.getId(), -1)) u.setIdEmploye(e.getId());
            });
        }
        u.getRoles().add(role("Employe"));
        userRepo.save(u);
        notificationService.notifierRole("ADMIN", "INSCRIPTION",
                "Nouvelle inscription à valider : " + nom + " (" + login + ")", "/admin/utilisateurs?statut=EN_ATTENTE");
        return toDto(u);
    }

    // ------------------------------------------------------------------ profil

    @Transactional(readOnly = true)
    public UtilisateurDto profil(Integer idUser) {
        return toDto(trouver(idUser));
    }

    public UtilisateurDto modifierProfil(Integer idUser, String nomComplet, String email) {
        User u = trouver(idUser);
        u.setNomComplet(obligatoire(nomComplet, "Le nom complet est obligatoire."));
        u.setEmail(controlerEmail(email, false));
        return toDto(u);
    }

    public void changerMotDePasse(Integer idUser, MotDePasseRequest req) {
        User u = trouver(idUser);
        if (req == null || !passwordService.verifier(req.ancien(), u.getMotDePasse()))
            throw new IllegalArgumentException("Le mot de passe actuel est incorrect.");
        if (req.nouveau() == null || !req.nouveau().equals(req.confirmation()))
            throw new IllegalArgumentException("Les deux nouveaux mots de passe ne correspondent pas.");
        passwordService.controlerRobustesse(req.nouveau());
        u.setMotDePasse(passwordService.hacher(req.nouveau()));
    }

    // ------------------------------------------------------------------ administration

    @Transactional(readOnly = true)
    public List<UtilisateurDto> lister(String statut, String recherche) {
        String s = statut == null ? "" : statut.trim().toUpperCase(Locale.ROOT);
        String q = recherche == null ? "" : recherche.trim().toLowerCase(Locale.ROOT);
        return userRepo.rechercher(s, q).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public UtilisateurDto fiche(Integer id) {
        return toDto(trouver(id));
    }

    @Transactional(readOnly = true)
    public List<OptionDto> roles() {
        return roleRepo.findAllByOrderByLibelleAsc().stream().map(r -> new OptionDto(r.getId(), r.getLibelle())).toList();
    }

    /** Comptes actifs, pour les listes de choix (délégation...). */
    @Transactional(readOnly = true)
    public List<OptionDto> actifs() {
        return userRepo.findByStatutOrderByLoginAsc("ACTIF").stream()
                .map(u -> new OptionDto(u.getId(), u.getLogin() + (u.getNomComplet() == null ? "" : " — " + u.getNomComplet())))
                .toList();
    }

    public UtilisateurDto creer(UtilisateurRequest req) {
        if (req == null) throw new IllegalArgumentException("Formulaire manquant.");
        User u = new User();
        u.setLogin(controlerLogin(req.login()));
        passwordService.controlerRobustesse(req.motDePasse());
        u.setMotDePasse(passwordService.hacher(req.motDePasse()));
        u.setStatut(req.statut() == null || req.statut().isBlank() ? "ACTIF" : statut(req.statut()));
        appliquer(u, req);
        userRepo.save(u);
        return toDto(u);
    }

    public UtilisateurDto modifier(Integer id, UtilisateurRequest req, Integer idAdmin) {
        if (req == null) throw new IllegalArgumentException("Formulaire manquant.");
        User u = trouver(id);
        appliquer(u, req);
        if (req.statut() != null && !req.statut().isBlank()) changerStatut(u, statut(req.statut()), idAdmin);
        return toDto(u);
    }

    /** Valide une inscription (ou réactive un compte) : statut ACTIF, l'inscrit est prévenu dans l'application. */
    public UtilisateurDto activer(Integer id, Integer idAdmin) {
        User u = trouver(id);
        boolean inscription = "EN_ATTENTE".equals(u.getStatut());
        changerStatut(u, "ACTIF", idAdmin);
        if (inscription) notificationService.notifier(u.getId(), "COMPTE_ACTIVE",
                "Bienvenue ! Votre compte a été validé par l'administrateur.", "/");
        return toDto(u);
    }

    public UtilisateurDto desactiver(Integer id, Integer idAdmin) {
        User u = trouver(id);
        changerStatut(u, "DESACTIVE", idAdmin);
        return toDto(u);
    }

    /** Génère un mot de passe provisoire (affiché une seule fois à l'administrateur). */
    public String reinitialiserMotDePasse(Integer id) {
        User u = trouver(id);
        String provisoire = genererProvisoire();
        u.setMotDePasse(passwordService.hacher(provisoire));
        return provisoire;
    }

    /** Une inscription refusée (jamais validée) est supprimée ; un compte qui a servi est désactivé. */
    public void supprimer(Integer id, Integer idAdmin) {
        User u = trouver(id);
        if (u.getId().equals(idAdmin)) throw new IllegalStateException("Vous ne pouvez pas supprimer votre propre compte.");
        if (!"EN_ATTENTE".equals(u.getStatut()) || u.getDerniereConnexion() != null)
            throw new IllegalStateException("Seule une inscription non validée peut être supprimée : désactivez plutôt ce compte.");
        userRepo.delete(u);
    }

    // ------------------------------------------------------------------ outils

    private void appliquer(User u, UtilisateurRequest req) {
        u.setNomComplet(obligatoire(req.nomComplet(), "Le nom complet est obligatoire."));
        u.setEmail(controlerEmail(req.email(), false));
        if (req.idEmploye() != null) {
            if (!employeRepo.existsById(req.idEmploye())) throw new IllegalArgumentException("Employé introuvable.");
            if (userRepo.existsByIdEmployeAndIdNot(req.idEmploye(), u.getId() == null ? -1 : u.getId()))
                throw new IllegalArgumentException("Cet employé a déjà un compte.");
        }
        u.setIdEmploye(req.idEmploye());
        if (req.roles() != null) {
            Set<Role> roles = new HashSet<>();
            for (String r : req.roles()) roles.add(role(r));
            if (roles.isEmpty()) throw new IllegalArgumentException("Choisissez au moins un rôle.");
            u.setRoles(roles);
        }
    }

    private void changerStatut(User u, String statut, Integer idAdmin) {
        if (u.getId().equals(idAdmin) && !"ACTIF".equals(statut))
            throw new IllegalStateException("Vous ne pouvez pas désactiver votre propre compte.");
        boolean admin = u.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getLibelle()));
        if (admin && !"ACTIF".equals(statut) && userRepo.findActifsParRole("ADMIN").size() <= 1)
            throw new IllegalStateException("Il doit rester au moins un administrateur actif.");
        u.setStatut(statut);
    }

    private Role role(String libelle) {
        return roleRepo.findByLibelle(libelle).orElseThrow(() -> new IllegalArgumentException("Rôle inconnu : " + libelle));
    }

    private String statut(String s) {
        String v = s.trim().toUpperCase(Locale.ROOT);
        if (!STATUTS.contains(v)) throw new IllegalArgumentException("Statut inconnu : " + s);
        return v;
    }

    private String controlerLogin(String login) {
        if (login == null || !login.trim().matches(LOGIN_REGEX))
            throw new IllegalArgumentException("Identifiant invalide : 3 à 50 caractères (lettres, chiffres, . _ -).");
        String l = login.trim();
        if (userRepo.existsByLoginIgnoreCase(l)) throw new IllegalArgumentException("Cet identifiant est déjà utilisé.");
        return l;
    }

    private static String controlerEmail(String email, boolean obligatoire) {
        if (email == null || email.isBlank()) {
            if (obligatoire) throw new IllegalArgumentException("L'adresse email est obligatoire.");
            return null;
        }
        String e = email.trim();
        if (!e.matches(EMAIL_REGEX)) throw new IllegalArgumentException("Adresse email invalide.");
        return e;
    }

    private static String obligatoire(String v, String message) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(message);
        return v.trim();
    }

    private static String genererProvisoire() {
        String lettres = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(lettres.charAt(ALEA.nextInt(lettres.length())));
        sb.append(10 + ALEA.nextInt(90));
        return sb.toString();
    }

    private User trouver(Integer id) {
        return userRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable : " + id));
    }

    private UtilisateurDto toDto(User u) {
        Employe e = u.getIdEmploye() == null ? null : employeRepo.findById(u.getIdEmploye()).orElse(null);
        Affectation a = e == null ? null : affectationRepo.findEnCoursParEmploye(e.getId()).stream().findFirst().orElse(null);
        return new UtilisateurDto(u.getId(), u.getLogin(), u.getNomComplet(), u.getEmail(), u.getStatut(),
                u.getRoles().stream().map(Role::getLibelle).sorted().toList(),
                u.getIdEmploye(), e == null ? null : e.getNom() + (e.getPrenoms() == null ? "" : " " + e.getPrenoms()),
                e == null ? null : e.getMatricule(), a == null ? null : a.getPoste().getLibelle(),
                u.getDateCreation(), u.getDerniereConnexion());
    }
}
