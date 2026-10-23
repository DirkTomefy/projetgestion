package s5.itu.projetgestion.dto.recrutement;

import java.time.LocalDate;

/** Délégation de validation : statut A_VENIR, EN_COURS ou TERMINEE. */
public record DelegationDto(Integer id, Integer idDelegant, String delegant, Integer idDelegue, String delegue,
                            LocalDate dateDebut, LocalDate dateFin, String statut) {
}
