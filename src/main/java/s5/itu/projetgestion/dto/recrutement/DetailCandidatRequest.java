package s5.itu.projetgestion.dto.recrutement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DetailCandidatRequest(
        @NotBlank(message = "Le type est obligatoire")
        String codeTypeInformation,

        @NotBlank(message = "Le libellé est obligatoire")
        @Size(max = 255, message = "255 caractères au maximum")
        String libelle,

        @PastOrPresent(message = "La date de début ne peut pas être dans le futur")
        LocalDate dateDebut,

        LocalDate dateFin) {

    @JsonIgnore
    @AssertTrue(message = "La date de fin doit être postérieure à la date de début")
    public boolean isPeriodeValide() {
        return dateDebut == null || dateFin == null || !dateFin.isBefore(dateDebut);
    }
}
