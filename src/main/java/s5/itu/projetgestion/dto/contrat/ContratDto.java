package s5.itu.projetgestion.dto.contrat;

import java.time.LocalDate;
import java.util.List;

public record ContratDto(
    Integer id, String reference, String employe, String matricule, String poste,
    String type, LocalDate dateDebut, LocalDate dateFinPrevue, LocalDate dateFinEffective,
    String statut, String motifFin, String referencePrecedent, boolean documentDepose,
    List<EtatContratDto> historique
) {}