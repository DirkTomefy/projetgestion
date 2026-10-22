package s5.itu.projetgestion.model.embauche;

import jakarta.persistence.*;

/**
 * Une condition de la proposition et sa valeur actuelle.
 * accepte : NULL = en attente de réponse du candidat, TRUE = acceptée, FALSE = refusée.
 * Les valeurs précédentes sont conservées dans relance_condition (ancienne_valeur).
 */
@Entity
@Table(name = "proposition_condition",
        uniqueConstraints = @UniqueConstraint(columnNames = { "id_proposition", "id_point_condition" }))
public class PropositionCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proposition", nullable = false)
    private PropositionEmbauche proposition;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_point_condition", nullable = false)
    private PointCondition pointCondition;

    @Column(columnDefinition = "TEXT")
    private String valeur;

    private Boolean accepte;

    @Column(name = "commentaire_candidat", columnDefinition = "TEXT")
    private String commentaireCandidat;

    public boolean enAttente() { return accepte == null; }
    public boolean estAcceptee() { return Boolean.TRUE.equals(accepte); }
    public boolean estRefusee() { return Boolean.FALSE.equals(accepte); }

    public String getCode() {
        return pointCondition != null ? pointCondition.getLibelle() : null;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public PropositionEmbauche getProposition() { return proposition; }
    public void setProposition(PropositionEmbauche proposition) { this.proposition = proposition; }
    public PointCondition getPointCondition() { return pointCondition; }
    public void setPointCondition(PointCondition pointCondition) { this.pointCondition = pointCondition; }
    public String getValeur() { return valeur; }
    public void setValeur(String valeur) { this.valeur = valeur; }
    public Boolean getAccepte() { return accepte; }
    public void setAccepte(Boolean accepte) { this.accepte = accepte; }
    public String getCommentaireCandidat() { return commentaireCandidat; }
    public void setCommentaireCandidat(String commentaireCandidat) { this.commentaireCandidat = commentaireCandidat; }
}
