package s5.itu.projetgestion.dto.recrutement;

import java.util.List;

public record NiveauxSelectionDto(Integer idOffreEmploi, boolean personnalises, List<NiveauSelectionDto> niveaux) {
}
