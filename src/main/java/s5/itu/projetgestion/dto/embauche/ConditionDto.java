package s5.itu.projetgestion.dto.embauche;

/**
 * Une condition de la proposition.
 * accepte : null = en attente, true = acceptée, false = refusée.
 * enContreProposition : la valeur actuelle vient d'une contre-proposition (ancienneValeur = valeur précédente).
 */
public record ConditionDto(Integer id, String code, String libelle, String valeur, String valeurAffichee,
                           Boolean accepte, String commentaireCandidat,
                           boolean enContreProposition, String ancienneValeurAffichee) {
}
