package s5.itu.projetgestion.service.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Salaire minimum et maximum d'un poste (catégorie du métier × échelon) dans la grille en vigueur. */
public record FourchetteSalariale(Integer idPoste, String categorie, String echelon,
                                  BigDecimal min, BigDecimal max, Integer idGrille, LocalDate dateEffectif) {

    public boolean contient(BigDecimal salaire) {
        return salaire != null && salaire.compareTo(min) >= 0 && salaire.compareTo(max) <= 0;
    }
}
