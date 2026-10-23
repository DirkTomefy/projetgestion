package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.recrutement.NiveauSelection;

import java.util.List;

public interface NiveauSelectionRepository extends JpaRepository<NiveauSelection, Integer> {

    List<NiveauSelection> findByIdOffreEmploiIsNullOrderByOrdreAsc();

    List<NiveauSelection> findByIdOffreEmploiOrderByOrdreAsc(Integer idOffreEmploi);
}
