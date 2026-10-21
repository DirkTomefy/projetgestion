package s5.itu.projetgestion.repository.personnel;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.User;
import s5.itu.projetgestion.model.personnel.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUtilisateurAndLuFalseOrderByDateCreationDesc(User utilisateur);
}