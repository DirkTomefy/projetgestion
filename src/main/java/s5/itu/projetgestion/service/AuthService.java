package s5.itu.projetgestion.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.err.AuthException;
import s5.itu.projetgestion.model.User;
import s5.itu.projetgestion.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String login, String motDePasse) {
        if (login == null || motDePasse == null) {
            throw new IllegalArgumentException("Login ou mot de passe incorrect");
        }
        User user = userRepository.findByLogin(login.trim())
                .orElseThrow(() -> new IllegalArgumentException("Login ou mot de passe incorrect"));

        //  Comparaison en clair — À REMPLACER par BCrypt avant mise en production
        if (!motDePasse.equals(user.getMotDePasse())) {
            throw new AuthException("Login ou mot de passe incorrect");
        }
        return user;
    }
}