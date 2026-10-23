package s5.itu.projetgestion.dto.besoin;

import s5.itu.projetgestion.dto.organisation.PosteDemandeRequest;

/**
 * Une ligne du besoin :
 *  - poste existant : idPoste (+ quantite, ou idEmployeRemplace pour un remplacement) ;
 *  - nouveau poste (besoin de type CREATION) : nouveauPoste, créé à l'état PROPOSE dans l'unité du besoin
 *    et activé quand le besoin est validé.
 */
public record LignePosteRequest(Integer idPoste, Integer quantite, Integer idEmployeRemplace, PosteDemandeRequest nouveauPoste) {

    public LignePosteRequest(Integer idPoste, Integer quantite, Integer idEmployeRemplace) {
        this(idPoste, quantite, idEmployeRemplace, null);
    }
}
