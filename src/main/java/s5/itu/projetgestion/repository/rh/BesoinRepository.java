package s5.itu.projetgestion.repository.rh;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.rh.besoin.Besoin;

public interface BesoinRepository extends JpaRepository<Besoin, Integer> {
    long countByReferenceStartingWith(String prefix);
    boolean existsByReference(String reference);
}