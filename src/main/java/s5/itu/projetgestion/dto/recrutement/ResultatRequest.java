package s5.itu.projetgestion.dto.recrutement;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResultatRequest(
        @NotNull(message = "La date de l'évaluation est obligatoire")
        @PastOrPresent(message = "La date ne peut pas être dans le futur")
        LocalDate dateRealisee,

        @DecimalMin(value = "0", message = "Le score est entre 0 et 20")
        @DecimalMax(value = "20", message = "Le score est entre 0 et 20")
        BigDecimal score,

        String decision,

        @Size(max = 2000, message = "2000 caractères au maximum")
        String commentaire) {
}
