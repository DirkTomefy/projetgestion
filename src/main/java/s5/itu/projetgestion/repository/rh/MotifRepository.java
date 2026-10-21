package s5.itu.projetgestion.repository.rh;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.rh.besoin.Motif;

public interface MotifRepository extends JpaRepository<Motif, Integer> {
    boolean existsByLibelleIgnoreCase(String libelle);   // éviter les doublons
}