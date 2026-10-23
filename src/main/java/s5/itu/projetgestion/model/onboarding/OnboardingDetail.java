package s5.itu.projetgestion.model.onboarding;

import java.time.LocalDate;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.model.organisation.Poste;

/** Tâche d'un onboarding : A_FAIRE, FAIT ou NON_APPLICABLE (interdit pour une tâche obligatoire). */
@Entity
@Table(name = "onboarding_details")
public class OnboardingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_onboarding", nullable = false)
    private Onboarding onboarding;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_workflow_details", nullable = false)
    private WorkflowOnboardingDetail modele;
    @Column(nullable = false)
    private Integer ordre;
    @Column(length = 255)
    private String libelle;
    @Column(name = "type_tache", length = 100)
    private String typeTache;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_poste_responsable")
    private Poste posteResponsable;
    @Column(nullable = false, length = 30)
    private String statut = "A_FAIRE";
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_realisateur")
    private User realisateur;
    @Column(name = "date_realisation")
    private LocalDate dateRealisation;
    @Column(columnDefinition = "TEXT")
    private String commentaire;
    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    public Integer getId() { return id; }
    public Onboarding getOnboarding() { return onboarding; }
    public void setOnboarding(Onboarding onboarding) { this.onboarding = onboarding; }
    public WorkflowOnboardingDetail getModele() { return modele; }
    public void setModele(WorkflowOnboardingDetail modele) { this.modele = modele; }
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public String getTypeTache() { return typeTache; }
    public void setTypeTache(String typeTache) { this.typeTache = typeTache; }
    public Poste getPosteResponsable() { return posteResponsable; }
    public void setPosteResponsable(Poste posteResponsable) { this.posteResponsable = posteResponsable; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public User getRealisateur() { return realisateur; }
    public void setRealisateur(User realisateur) { this.realisateur = realisateur; }
    public LocalDate getDateRealisation() { return dateRealisation; }
    public void setDateRealisation(LocalDate dateRealisation) { this.dateRealisation = dateRealisation; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }

    /** Obligatoire selon le modèle. */
    public boolean isObligatoire() { return modele == null || modele.isObligatoire(); }
}
