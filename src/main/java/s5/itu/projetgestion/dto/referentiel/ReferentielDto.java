package s5.itu.projetgestion.dto.referentiel;

/**
 * Élément d'un référentiel. Champs facultatifs selon le référentiel :
 * idParent / parent (catégorie d'un métier), dureeMax / renouvellementMax (type de contrat).
 * systeme = valeur utilisée par l'application : ni renommage ni suppression.
 */
public record ReferentielDto(Integer id, String libelle, Integer idParent, String parent,
                             Integer dureeMax, Integer renouvellementMax, boolean systeme) {

    public static ReferentielDto simple(Integer id, String libelle, boolean systeme) {
        return new ReferentielDto(id, libelle, null, null, null, null, systeme);
    }
}
