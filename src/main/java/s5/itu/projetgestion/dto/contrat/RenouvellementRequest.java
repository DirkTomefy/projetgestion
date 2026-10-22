package s5.itu.projetgestion.dto.contrat;

import java.time.LocalDate;

public record RenouvellementRequest(
    Integer idTypeNouveau,     // null = même type (renouvellement)
    LocalDate dateFinPrevue,   // null seulement pour un CDI
    String motif               // facultatif
) {}