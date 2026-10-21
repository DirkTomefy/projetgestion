package s5.itu.projetgestion.dto;

import s5.itu.projetgestion.model.rh.organisation.Poste;

// Ce que l'API renvoie après création du poste
public record PosteDto(
    Integer id, String code, String libelle, String etat,
    String statutOccupation, String metier, String categorie,
    String echelon, String unite
) {
    public static PosteDto from(Poste p) {
        return new PosteDto(
            p.getId(), p.getCode(), p.getLibelle(), p.getEtat(), p.getStatutOccupation(),
            p.getMetier().getLibelle(),
            p.getMetier().getCategorie().getLibelle(),
            p.getEchelon().getLibelle(),
            p.getUniteOrga().getLibelle());
    }
}