package s5.itu.projetgestion.repository.organisation;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.organisation.EchelonMetier;

public interface EchelonMetierRepository extends JpaRepository<EchelonMetier, Integer> {
}