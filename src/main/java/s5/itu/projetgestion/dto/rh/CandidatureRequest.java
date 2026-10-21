package s5.itu.projetgestion.dto.rh;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CandidatureRequest(
        @NotNull(message = "L'offre est obligatoire")
        Integer idOffreEmploi,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "100 caractères au maximum")
        String nom,

        @Size(max = 150, message = "150 caractères au maximum")
        String prenom,

        @NotNull(message = "La date de naissance est obligatoire")
        @Past(message = "La date de naissance doit être dans le passé")
        LocalDate dateNaissance,

        @Size(max = 255, message = "255 caractères au maximum")
        String adresse,

        @NotBlank(message = "Le téléphone est obligatoire")
        @Pattern(regexp = "^[+0-9 ().-]{7,30}$", message = "Numéro de téléphone invalide")
        String telephone,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Adresse email invalide")
        @Size(max = 150, message = "150 caractères au maximum")
        String email,

        @Valid
        @Size(max = 30, message = "30 lignes au maximum")
        List<DetailCandidatRequest> details) {

    public List<DetailCandidatRequest> detailsOuVide() {
        return details == null ? List.of() : details;
    }
}
