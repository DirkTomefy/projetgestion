package s5.itu.projetgestion.repository.postes;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.postes.Affectation;
import s5.itu.projetgestion.model.postes.Employe;

public interface AffectationRepository extends JpaRepository<Affectation, Integer>{
    
    public Affectation findByEmploye(Employe employe);

}
