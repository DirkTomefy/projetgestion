package s5.itu.projetgestion.model.besoin;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.auth.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "validation")
public class Validation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_besoin", nullable = false)
    private Besoin besoin;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_workflow_validation", nullable = false)
    private WorkflowValidation workflowValidation;
    @Column(nullable = false, length = 30)
    private String decision;
    @Column(nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    private String commentaire;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User value) {
        user = value;
    }

    public Besoin getBesoin() {
        return besoin;
    }

    public void setBesoin(Besoin value) {
        besoin = value;
    }

    public WorkflowValidation getWorkflowValidation() {
        return workflowValidation;
    }

    public void setWorkflowValidation(WorkflowValidation value) {
        workflowValidation = value;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String value) {
        decision = value;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime value) {
        date = value;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String value) {
        commentaire = value;
    }
}
