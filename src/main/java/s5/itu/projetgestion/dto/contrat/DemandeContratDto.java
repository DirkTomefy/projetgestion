package s5.itu.projetgestion.dto.contrat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DemandeContratDto(
        Integer id, String reference, String etat, String origine, String motifType,
        Integer idEmploye, String employe, String matricule, String poste, String unite,
        Integer idContrat, String contratRef, String typeActuel, LocalDate debutActuel, LocalDate finActuelle,
        BigDecimal salaireActuel,
        String typeDemande, LocalDate dateEffet, LocalDate dateFinPrevue, BigDecimal salaire, String motif,
        String demandeur, LocalDateTime dateDemande,
        boolean depassementBudget, BigDecimal surcoutAnnuel, BigDecimal disponible,
        BigDecimal salaireMin, BigDecimal salaireMax,
        List<Etape> etapes, Integer idContratResultat, String contratResultatRef,
        boolean peutInstruire, boolean peutFinance, boolean peutDecider, boolean peutAnnuler) {

    public record Etape(String etape, String par, LocalDateTime date, String commentaire) { }
}
