package s5.itu.projetgestion.repository.recrutement;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.recrutement.TypeInformation;

import java.util.List;
import java.util.Optional;

public interface TypeInformationRepository extends JpaRepository<TypeInformation, Integer> {
    Optional<TypeInformation> findByLibelle(String libelle);
    List<TypeInformation> findAllByOrderByIdAsc();
}
