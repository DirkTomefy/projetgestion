package s5.itu.projetgestion.dto.rh;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BesoinOffreInfo(
        Integer id,
        String reference,
        String typeBesoin,
        Integer effectif,
        LocalDate dateSouhaitee,
        String etatActuel,
        String precisionMotif,
        String motif,
        String unite,
        String uniteType,
        String poste,
        String metier,
        String categorie,
        String echelon,
        BigDecimal salaireMin,
        BigDecimal salaireMax
) { }
