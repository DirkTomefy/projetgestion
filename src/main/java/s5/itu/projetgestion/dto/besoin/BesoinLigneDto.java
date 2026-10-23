package s5.itu.projetgestion.dto.besoin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Ligne de la liste des besoins. etapeCourante : poste(s) attendu(s) pour la prochaine validation. */
public record BesoinLigneDto(Integer id, String reference, String typeBesoin, Integer idUnite, String unite,
                             String postes, Integer effectif, LocalDate dateSouhaitee, String etat,
                             String demandeur, LocalDateTime dateDemande, String etapeCourante,
                             boolean aValiderParMoi, boolean nouveauPoste, BigDecimal coutAnnuel) {
}
