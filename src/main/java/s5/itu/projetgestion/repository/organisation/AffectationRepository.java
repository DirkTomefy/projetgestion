package s5.itu.projetgestion.repository.organisation;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.organisation.Affectation;
import s5.itu.projetgestion.model.organisation.Employe;

public interface AffectationRepository extends JpaRepository<Affectation, Integer>{
    
    public Affectation findByEmploye(Employe employe);

}
