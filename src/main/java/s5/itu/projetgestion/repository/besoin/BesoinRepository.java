package s5.itu.projetgestion.repository.besoin;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.besoin.Besoin;

public interface BesoinRepository extends JpaRepository<Besoin, Integer> {

	@Query("""
			SELECT DISTINCT b
			FROM Besoin b
			JOIN FETCH b.motif
			JOIN FETCH b.uniteOrga
			JOIN FETCH b.user u
			LEFT JOIN FETCH u.roles
			WHERE b.id = :id
			""")
	Optional<Besoin> findByIdWithRelations(@Param("id") Integer id);

	long countByReferenceStartingWith(String prefix);

	boolean existsByReference(String reference);
}
