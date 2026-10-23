package s5.itu.projetgestion.repository.organisation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.organisation.CoordonneesBancaires;

public interface CoordonneesBancairesRepository extends JpaRepository<CoordonneesBancaires, Integer> {
    List<CoordonneesBancaires> findByEmployeIdOrderByDateEffetDescIdDesc(Integer idEmploye);
}
