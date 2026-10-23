package s5.itu.projetgestion.repository.auth;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.auth.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByLogin(String login);

    boolean existsByLoginIgnoreCase(String login);

    boolean existsByIdEmployeAndIdNot(Integer idEmploye, Integer id);

    /** Comptes filtrés par statut ('' = tous) et texte (login, nom, email). */
    @Query("""
        select distinct u from User u left join fetch u.roles
        where (:statut = '' or u.statut = :statut)
          and (:q = '' or lower(concat(u.login, ' ', coalesce(u.nomComplet, ''), ' ', coalesce(u.email, ''))) like concat('%', :q, '%'))
        order by u.login
        """)
    List<User> rechercher(@Param("statut") String statut, @Param("q") String q);

    long countByStatut(String statut);

    Optional<User> findFirstByIdEmploye(Integer idEmploye);

    /** Comptes actifs ayant un rôle donné (destinataires de notifications, listes de choix). */
    @Query("select u from User u join u.roles r where r.libelle = :role and u.statut = 'ACTIF' order by u.login")
    List<User> findActifsParRole(@Param("role") String role);

    List<User> findByStatutOrderByLoginAsc(String statut);
}
