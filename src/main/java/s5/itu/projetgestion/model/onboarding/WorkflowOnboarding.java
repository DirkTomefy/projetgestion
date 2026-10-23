package s5.itu.projetgestion.model.onboarding;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

/** Modèle d'onboarding : liste ordonnée de tâches. Statut ACTIF ou INACTIF. */
@Entity
@Table(name = "workflow_onboarding")
public class WorkflowOnboarding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, length = 150)
    private String libelle;
    @Column(length = 30)
    private String statut = "ACTIF";
    @OneToMany(mappedBy = "workflow")
    @OrderBy("ordre ASC")
    private List<WorkflowOnboardingDetail> taches = new ArrayList<>();

    public Integer getId() { return id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public List<WorkflowOnboardingDetail> getTaches() { return taches; }
}
