package s5.itu.projetgestion.repository.auth;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.auth.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByLibelle(String libelle);
    List<Role> findAllByOrderByLibelleAsc();
}
