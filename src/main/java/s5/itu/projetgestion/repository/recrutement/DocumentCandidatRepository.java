package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.recrutement.DocumentCandidat;

import java.util.List;

public interface DocumentCandidatRepository extends JpaRepository<DocumentCandidat, Integer> {
    List<DocumentCandidat> findByCandidature_IdOrderByIdAsc(Integer idCandidature);
}
