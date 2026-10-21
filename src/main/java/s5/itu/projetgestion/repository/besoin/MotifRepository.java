package s5.itu.projetgestion.repository.besoin;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.besoin.Motif;

public interface MotifRepository extends JpaRepository<Motif, Integer>{
    
}
