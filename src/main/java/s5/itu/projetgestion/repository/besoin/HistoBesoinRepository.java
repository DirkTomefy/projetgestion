package s5.itu.projetgestion.repository.besoin;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import s5.itu.projetgestion.model.besoin.HistoBesoin;

public interface HistoBesoinRepository extends JpaRepository<HistoBesoin, Integer> {

    @Query("select h from HistoBesoin h left join fetch h.user where h.besoin.id = :idBesoin order by h.dateAction, h.id")
    List<HistoBesoin> findParBesoin(@Param("idBesoin") Integer idBesoin);
}
