package s5.itu.projetgestion.repository.contrat;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.contrat.EtatContrat;

public interface EtatContratRepository extends JpaRepository<EtatContrat, Integer> {
    List<EtatContrat> findByContratIdOrderByIdAsc(Integer idContrat);
}