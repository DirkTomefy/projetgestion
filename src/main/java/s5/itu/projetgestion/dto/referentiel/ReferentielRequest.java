package s5.itu.projetgestion.dto.referentiel;

public record ReferentielRequest(String libelle, Integer idParent, Integer dureeMax, Integer renouvellementMax) {
}
