package s5.itu.projetgestion.model.onboarding;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.contrat.Contrat;

/** Onboarding d'un nouvel employé, démarré à la signature de son contrat. Statut EN_COURS ou TERMINE. */
@Entity
@Table(name = "onboarding")
public class Onboarding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrat", nullable = false)
    private Contrat contrat;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_workflow", nullable = false)
    private WorkflowOnboarding workflow;
    @Column(nullable = false, length = 30)
    private String statut = "EN_COURS";
    @Column(name = "date_debut")
    private LocalDate dateDebut;
    @Column(name = "date_fin")
    private LocalDate dateFin;
    @OneToMany(mappedBy = "onboarding")
    @OrderBy("ordre ASC")
    private List<OnboardingDetail> taches = new ArrayList<>();

    public Integer getId() { return id; }
    public Contrat getContrat() { return contrat; }
    public void setContrat(Contrat contrat) { this.contrat = contrat; }
    public WorkflowOnboarding getWorkflow() { return workflow; }
    public void setWorkflow(WorkflowOnboarding workflow) { this.workflow = workflow; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public List<OnboardingDetail> getTaches() { return taches; }
}
