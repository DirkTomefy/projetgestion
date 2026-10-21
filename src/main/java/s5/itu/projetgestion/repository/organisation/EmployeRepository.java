package s5.itu.projetgestion.repository.organisation;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.organisation.Employe;

public interface EmployeRepository extends JpaRepository<Employe, Integer> {
    
}
