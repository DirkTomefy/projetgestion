package s5.itu.projetgestion.repository.finance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.finance.GrilleSalarialeDetail;

public interface GrilleSalarialeDetailRepository extends JpaRepository<GrilleSalarialeDetail, Integer> {

    @Query("""
            select d from GrilleSalarialeDetail d join fetch d.categorie c join fetch d.echelon e
            where d.grille.id = :idGrille order by c.libelle, e.id
            """)
    List<GrilleSalarialeDetail> findParGrille(@Param("idGrille") Integer idGrille);
}
