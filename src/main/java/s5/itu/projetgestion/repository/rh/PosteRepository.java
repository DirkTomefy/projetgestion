package s5.itu.projetgestion.repository.rh;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.rh.organisation.Poste;

public interface PosteRepository extends JpaRepository<Poste, Integer> {
    List<Poste> findByEtatAndUniteOrgaId(String etat, Integer idUnite);  // formulaire de besoin
    List<Poste> findByEtat(String etat);                                  // postes PROPOSE (P1)
    boolean existsByCode(String code);
}