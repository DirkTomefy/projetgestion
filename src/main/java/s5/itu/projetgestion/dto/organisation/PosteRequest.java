package s5.itu.projetgestion.dto.organisation;

/** Création directe (ADMIN / RH) ou modification d'un poste. */
public record PosteRequest(String libelle, Integer idMetier, Integer idEchelon, Integer idUniteOrga, Integer idSuperieurDirect) {
}
