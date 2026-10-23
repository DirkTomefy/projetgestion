package s5.itu.projetgestion.dto.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record GrilleRequest(LocalDate dateEffectif, List<Ligne> lignes) {
    public record Ligne(Integer idCategorie, Integer idEchelon, BigDecimal montantMin, BigDecimal montantMax) { }
}
