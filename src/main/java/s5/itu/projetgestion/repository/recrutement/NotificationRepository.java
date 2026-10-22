package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.model.recrutement.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUtilisateurAndLuFalseOrderByDateCreationDesc(User utilisateur);
}