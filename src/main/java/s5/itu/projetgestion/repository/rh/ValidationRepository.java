package s5.itu.projetgestion.repository.rh;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.rh.besoin.Validation;



public interface ValidationRepository extends JpaRepository<Validation, Integer> {}