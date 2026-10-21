package s5.itu.projetgestion.dto.besoin;

import java.time.LocalDate;
import java.util.List;

public record BesoinRequest(
    String typeBesoin,
    Integer idUniteOrga,
    List<LignePosteRequest> lignes,   // remplace idPostes et effectif
    Integer idMotif,
    LocalDate dateSouhaitee,
    String precisionMotif
) {}