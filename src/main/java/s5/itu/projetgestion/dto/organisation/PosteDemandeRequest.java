package s5.itu.projetgestion.dto.organisation;

// Ce que le formulaire envoie
public record PosteDemandeRequest(
    Integer idMetier,              // métier existant (peut être null)
    String nouveauMetierLibelle,   // OU nouveau métier
    Integer idCategorie,           // obligatoire si nouveau métier
    Integer idEchelon,
    Integer idUniteOrga,
    Integer idSuperieurDirect,     // peut être null (ex. poste racine)
    String libelle
) {}