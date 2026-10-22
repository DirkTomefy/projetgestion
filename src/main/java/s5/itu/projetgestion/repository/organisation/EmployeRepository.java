package s5.itu.projetgestion.repository.organisation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import s5.itu.projetgestion.model.organisation.Employe;

public interface EmployeRepository extends JpaRepository<Employe, Integer> {
    @Query(value = "SELECT nextval('employe_id_seq')", nativeQuery = true)
    Long getNextSequenceValue();
}
