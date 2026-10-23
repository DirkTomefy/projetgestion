package s5.itu.projetgestion.dto.organisation;

/** Corps de la requête PUT /api/postes/{id}/etat : { "etat": "ACTIF" } */
public record EtatRequest(String etat) {
}