package s5.itu.projetgestion.dto.organisation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import s5.itu.projetgestion.dto.commun.OptionDto;

/** Fiche d'un poste : rattachement, occupant, fourchette salariale, subordonnés, besoin d'origine. */
public record PosteDetailDto(
        Integer id, String code, String libelle, String etat, String statutOccupation,
        Integer idMetier, String metier, Integer idCategorie, String categorie, Integer idEchelon, String echelon,
        Integer idUnite, String unite, Integer idSuperieur, String superieur,
        Integer idEmploye, String occupant, String matricule, LocalDate depuis,
        BigDecimal salaireMin, BigDecimal salaireMax,
        List<OptionDto> subordonnes, String besoinReference, Integer idBesoin) {
}
