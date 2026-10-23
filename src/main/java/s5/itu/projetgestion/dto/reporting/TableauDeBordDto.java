package s5.itu.projetgestion.dto.reporting;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Indicateurs RH : chiffres clés et répartitions (libellé → valeur). */
public record TableauDeBordDto(Map<String, BigDecimal> chiffres,
                               List<Serie> besoinsParEtat, List<Serie> entonnoir, List<Serie> candidaturesParEtat,
                               List<Serie> contratsParType, List<Serie> effectifParCategorie, List<Serie> embauchesParMois,
                               List<Unite> unites) {

    public record Serie(String libelle, BigDecimal valeur) { }

    public record Unite(Integer id, String code, String libelle, String type, Integer idUniteMere, long postes, long occupes,
                        long vacants, long effectif, BigDecimal masseAnnuelle, BigDecimal budget, BigDecimal besoinsEnCours) { }
}
