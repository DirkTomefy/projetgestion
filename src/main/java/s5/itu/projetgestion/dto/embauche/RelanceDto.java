package s5.itu.projetgestion.dto.embauche;

import java.time.LocalDateTime;

public record RelanceDto(Integer id, Integer idCondition, String code, String libelle,
                         String ancienneValeur, String nouvelleValeur, String statut,
                         LocalDateTime date, Integer idUser, String commentaire) {
}
