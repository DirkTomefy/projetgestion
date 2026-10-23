package s5.itu.projetgestion.model.organisation;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Fiche de poste : rattachée à un métier (modèle réutilisable) ou à un poste précis (prime sur le métier). */
@Entity
@Table(name = "fiche_poste")
public class FichePoste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_metier")
    private Integer idMetier;

    @Column(name = "id_poste")
    private Integer idPoste;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mission;
    @Column(columnDefinition = "TEXT")
    private String activites;
    @Column(columnDefinition = "TEXT")
    private String formation;
    @Column(columnDefinition = "TEXT")
    private String experience;
    @Column(columnDefinition = "TEXT")
    private String competences;
    @Column(columnDefinition = "TEXT")
    private String conditions;

    @Column(nullable = false)
    private int version = 1;

    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj = LocalDateTime.now();

    @Column(name = "id_auteur")
    private Integer idAuteur;

    public Integer getId() { return id; }
    public Integer getIdMetier() { return idMetier; }
    public void setIdMetier(Integer v) { idMetier = v; }
    public Integer getIdPoste() { return idPoste; }
    public void setIdPoste(Integer v) { idPoste = v; }
    public String getMission() { return mission; }
    public void setMission(String v) { mission = v; }
    public String getActivites() { return activites; }
    public void setActivites(String v) { activites = v; }
    public String getFormation() { return formation; }
    public void setFormation(String v) { formation = v; }
    public String getExperience() { return experience; }
    public void setExperience(String v) { experience = v; }
    public String getCompetences() { return competences; }
    public void setCompetences(String v) { competences = v; }
    public String getConditions() { return conditions; }
    public void setConditions(String v) { conditions = v; }
    public int getVersion() { return version; }
    public void setVersion(int v) { version = v; }
    public LocalDateTime getDateMaj() { return dateMaj; }
    public void setDateMaj(LocalDateTime v) { dateMaj = v; }
    public Integer getIdAuteur() { return idAuteur; }
    public void setIdAuteur(Integer v) { idAuteur = v; }
}
