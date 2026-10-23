package s5.itu.projetgestion.dto.organisation;

import java.math.BigDecimal;
import java.util.List;

/** Unité organisationnelle avec ses indicateurs ; enfants rempli seulement dans l'organigramme. */
public record UniteDto(Integer id, String code, String type, String libelle,
                       Integer idUniteMere, String uniteMere, Integer idPosteResponsable, String posteResponsable,
                       String responsable, long postes, long occupes, long vacants, long effectif,
                       BigDecimal budget, List<UniteDto> enfants) {
}
