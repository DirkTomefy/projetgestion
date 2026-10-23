package s5.itu.projetgestion.dto.organisation;

import java.time.LocalDate;

public record CoordonneesBancairesDto(Integer id, String banque, String agence, String titulaire, String rib,
                                      LocalDate dateEffet, Boolean actif) {
}
