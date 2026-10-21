package s5.itu.projetgestion.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.auth.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByLogin(String login);

}