package s5.itu.projetgestion.dto.organisation;

/** Étape du circuit de validation des besoins. */
public record EtapeValidationDto(Integer id, Integer ordre, Integer idPoste, String poste, String unite,
                                 String occupant, String controle) {
}
