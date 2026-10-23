package s5.itu.projetgestion.model.onboarding;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.organisation.Poste;

/** Tâche d'un modèle d'onboarding. Poste responsable null = supérieur direct du nouvel employé. */
@Entity
@Table(name = "workflow_onboarding_details")
public class WorkflowOnboardingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_workflow", nullable = false)
    private WorkflowOnboarding workflow;
    @Column(nullable = false)
    private Integer ordre;
    @Column(name = "type_tache", nullable = false, length = 100)
    private String typeTache;
    @Column(length = 255)
    private String libelle;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_poste_responsable")
    private Poste posteResponsable;
    @Column(name = "delai_jours")
    private Integer delaiJours;
    @Column(nullable = false)
    private boolean obligatoire = true;

    public Integer getId() { return id; }
    public WorkflowOnboarding getWorkflow() { return workflow; }
    public void setWorkflow(WorkflowOnboarding workflow) { this.workflow = workflow; }
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
    public String getTypeTache() { return typeTache; }
    public void setTypeTache(String typeTache) { this.typeTache = typeTache; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public Poste getPosteResponsable() { return posteResponsable; }
    public void setPosteResponsable(Poste posteResponsable) { this.posteResponsable = posteResponsable; }
    public Integer getDelaiJours() { return delaiJours; }
    public void setDelaiJours(Integer delaiJours) { this.delaiJours = delaiJours; }
    public boolean isObligatoire() { return obligatoire; }
    public void setObligatoire(boolean obligatoire) { this.obligatoire = obligatoire; }
}
