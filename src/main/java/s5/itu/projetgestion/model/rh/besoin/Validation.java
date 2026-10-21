package s5.itu.projetgestion.model.rh.besoin;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.User;

@Entity
@Table(name = "validation")
public class Validation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_besoin")
    private Besoin besoin;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_workflow_validation")
    private WorkflowValidation workflowValidation;

    @Column(nullable = false, length = 30)
    private String decision;            // EN_ATTENTE, VALIDE, REFUSE

    // la base met NOW() toute seule
    @Column(insertable = false, updatable = false)
    private LocalDateTime date;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user2) { this.user = user2; }
    public Besoin getBesoin() { return besoin; }
    public void setBesoin(Besoin besoin) { this.besoin = besoin; }
    public WorkflowValidation getWorkflowValidation() { return workflowValidation; }
    public void setWorkflowValidation(WorkflowValidation workflowValidation) { this.workflowValidation = workflowValidation; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public LocalDateTime getDate() { return date; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}