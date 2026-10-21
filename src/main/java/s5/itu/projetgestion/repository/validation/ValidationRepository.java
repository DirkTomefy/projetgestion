package s5.itu.projetgestion.repository.validation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import s5.itu.projetgestion.model.besoin.Besoin;
import s5.itu.projetgestion.model.validation.Validation;

public interface ValidationRepository extends JpaRepository<Validation, Integer>{
    
    Optional<Validation> findFirstByBesoinOrderByDateDesc(Besoin besoin);

}
