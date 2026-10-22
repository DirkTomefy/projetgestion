package s5.itu.projetgestion.dto.embauche;

import java.math.BigDecimal;

/** Un siège (besoin_poste) du besoin de l'offre, avec sa fourchette salariale. */
public record SiegeDto(Integer idBesoinPoste, Integer idPoste, String codePoste, String poste, String unite,
                       String categorie, String echelon, String etat,
                       BigDecimal salaireMin, BigDecimal salaireMax,
                       boolean disponible, String indisponibilite) {
}
