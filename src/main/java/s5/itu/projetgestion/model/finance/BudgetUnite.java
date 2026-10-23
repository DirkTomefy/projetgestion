package s5.itu.projetgestion.model.finance;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.model.organisation.UniteOrga;

/**
 * Budget annuel d'une unité. La Finance propose un montant (montantPropose, statut A_VALIDER),
 * le DG le valide (il devient montant, statut VALIDE) ou le refuse.
 * montant = dernier montant validé, seul utilisé par le contrôle budgétaire (null s'il n'y en a jamais eu).
 */
@Entity
@Table(name = "budget_unite")
public class BudgetUnite {

    public static final String A_VALIDER = "A_VALIDER";
    public static final String VALIDE = "VALIDE";
    public static final String REFUSE = "REFUSE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_unite_orga", nullable = false)
    private UniteOrga uniteOrga;

    @Column
    private Integer exercice;

    @Column
    private Double montant;

    @Column(name = "montant_propose")
    private Double montantPropose;

    @Column(nullable = false, length = 20)
    private String statut = VALIDE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_propose")
    private User proposePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_decision")
    private User decidePar;

    @Column(name = "date_decision")
    private LocalDateTime dateDecision;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public UniteOrga getUniteOrga() { return uniteOrga; }
    public void setUniteOrga(UniteOrga uniteOrga) { this.uniteOrga = uniteOrga; }
    public Integer getExercice() { return exercice; }
    public void setExercice(Integer exercice) { this.exercice = exercice; }
    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }
    public Double getMontantPropose() { return montantPropose; }
    public void setMontantPropose(Double montantPropose) { this.montantPropose = montantPropose; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public User getProposePar() { return proposePar; }
    public void setProposePar(User proposePar) { this.proposePar = proposePar; }
    public User getDecidePar() { return decidePar; }
    public void setDecidePar(User decidePar) { this.decidePar = decidePar; }
    public LocalDateTime getDateDecision() { return dateDecision; }
    public void setDateDecision(LocalDateTime dateDecision) { this.dateDecision = dateDecision; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
