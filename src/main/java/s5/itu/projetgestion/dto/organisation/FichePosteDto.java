package s5.itu.projetgestion.dto.organisation;

import java.time.LocalDateTime;

/**
 * Fiche de poste. portee : METIER (modèle réutilisable), POSTE (fiche propre au poste) ou STANDARD (rien d'enregistré :
 * texte standard de départ). herite : la fiche affichée pour un poste vient du modèle de son métier.
 */
public record FichePosteDto(Integer id, String portee, Integer idMetier, String metier, Integer idPoste, String poste,
                            String mission, String activites, String formation, String experience,
                            String competences, String conditions, Integer version, LocalDateTime dateMaj,
                            boolean herite) { }
