package s5.itu.projetgestion.service.auth;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.err.AuthException;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.repository.auth.UserRepository;

@Service
@Transactional
public class AuthService {

    private static final String ECHEC = "Login ou mot de passe incorrect";

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public AuthService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    /**
     * Vérifie login et mot de passe, puis le statut du compte.
     * Un mot de passe encore stocké en clair est re-haché au passage.
     */
    public User authenticate(String login, String motDePasse) {
        if (login == null || login.isBlank() || motDePasse == null) throw new AuthException(ECHEC);
        User user = userRepository.findByLogin(login.trim()).orElseThrow(() -> new AuthException(ECHEC));
        if (!passwordService.verifier(motDePasse, user.getMotDePasse())) throw new AuthException(ECHEC);

        if ("EN_ATTENTE".equals(user.getStatut()))
            throw new AuthException("Votre inscription n'a pas encore été validée par un administrateur.");
        if ("DESACTIVE".equals(user.getStatut()))
            throw new AuthException("Ce compte est désactivé. Contactez l'administrateur.");

        if (!passwordService.estHache(user.getMotDePasse())) user.setMotDePasse(passwordService.hacher(motDePasse));
        user.setDerniereConnexion(LocalDateTime.now());
        return user;
    }
}
