package s5.itu.projetgestion.repository.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import s5.itu.projetgestion.model.notification.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    @Query("""
            SELECT n FROM Notification n
            WHERE n.utilisateur.id = :idUser AND (:nonLues = FALSE OR n.lu = FALSE)
            ORDER BY n.dateCreation DESC, n.id DESC
            """)
    List<Notification> findPourUtilisateur(@Param("idUser") Integer idUser, @Param("nonLues") boolean nonLues,
                                           Pageable page);

    long countByUtilisateur_IdAndLuFalse(Integer idUser);

    Optional<Notification> findByIdAndUtilisateur_Id(Integer id, Integer idUser);

    boolean existsByUtilisateur_IdAndTypeAndLien(Integer idUser, String type, String lien);

    @Modifying
    @Query("UPDATE Notification n SET n.lu = TRUE WHERE n.utilisateur.id = :idUser AND n.lu = FALSE")
    int marquerToutLu(@Param("idUser") Integer idUser);

    /** Utilisateurs ayant un rôle (libellé de la table role : RH, Finance, DG, Manager, ADMIN...). */
    @Query("SELECT DISTINCT u.id FROM User u JOIN u.roles r WHERE r.libelle = :role")
    List<Integer> findIdsUtilisateursParRole(@Param("role") String role);

    /** Utilisateurs dont l'employé occupe aujourd'hui le poste (affectation en cours). */
    @Query(nativeQuery = true, value = """
            SELECT DISTINCT u.id FROM "user" u
            JOIN affectation a ON a.id_employe = u.id_employe
            WHERE a.id_poste = :idPoste
              AND a.date_debut <= CURRENT_DATE
              AND (a.date_fin IS NULL OR a.date_fin >= CURRENT_DATE)
            """)
    List<Integer> findIdsUtilisateursParPoste(@Param("idPoste") Integer idPoste);

    /** Délégations en cours : [id du délégué, login du délégant] pour les délégants donnés. */
    @Query(nativeQuery = true, value = """
            SELECT d.id_user_delegue AS "idDelegue", u.login AS "delegant"
            FROM delegation d JOIN "user" u ON u.id = d.id_user_delegant
            WHERE d.id_user_delegant IN (:idsDelegants)
              AND CURRENT_DATE BETWEEN d.date_debut AND d.date_fin
            """)
    List<DelegationRow> findDelegationsEnCours(@Param("idsDelegants") List<Integer> idsDelegants);

    interface DelegationRow {
        Integer getIdDelegue();
        String getDelegant();
    }
}
