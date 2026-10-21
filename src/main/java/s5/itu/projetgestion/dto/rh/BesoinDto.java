package s5.itu.projetgestion.dto.rh;

import java.time.LocalDate;

import s5.itu.projetgestion.model.besoin.Besoin;

public record BesoinDto(
    Integer id, String reference, String typeBesoin, String poste, String motif,
    String unite, LocalDate dateSouhaitee, Integer effectif, String etatActuel,
    String demandeur
) {
    public static BesoinDto from(Besoin b, String poste) {
        return new BesoinDto(b.getId(), b.getReference(), b.getTypeBesoin(), poste,
            b.getMotif().getLibelle(), b.getUniteOrga().getLibelle(),
            b.getDateSouhaitee(), b.getEffectif(), b.getEtatActuel(),
            b.getUser().getLogin());
    }
}