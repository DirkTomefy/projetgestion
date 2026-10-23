package s5.itu.projetgestion.model.finance;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.auth.User;

/**
 * Grille salariale. Proposée par la Finance (A_VALIDER), elle ne s'applique qu'une fois VALIDEE par le DG ;
 * la grille alors en vigueur se termine la veille de sa date d'effet.
 */
@Entity
@Table(name = "grille_salariale")
public class GrilleSalariale {

    public static final String A_VALIDER = "A_VALIDER";
    public static final String VALIDEE = "VALIDEE";
    public static final String REFUSEE = "REFUSEE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "date_effectif", nullable = false)
    private LocalDate dateEffectif;
    @Column(name = "date_fin")
    private LocalDate dateFin;
    @Column(nullable = false, length = 20)
    private String statut = VALIDEE;
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
    public LocalDate getDateEffectif() { return dateEffectif; }
    public void setDateEffectif(LocalDate value) { dateEffectif = value; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate value) { dateFin = value; }
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
