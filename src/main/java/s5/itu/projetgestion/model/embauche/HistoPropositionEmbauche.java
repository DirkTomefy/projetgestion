package s5.itu.projetgestion.model.embauche;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Une ligne par événement de la proposition (envoi, réponse, contre-proposition, acceptation,
 * refus, expiration). statut = statut de la proposition après l'événement.
 * idUser NULL : action du candidat depuis la page publique, ou tâche automatique.
 */
@Entity
@Table(name = "histo_proposition_embauche")
public class HistoPropositionEmbauche {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proposition_embauche", nullable = false)
    private PropositionEmbauche propositionEmbauche;

    @Column(name = "id_statut", nullable = false, length = 20)
    private String statut;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(name = "id_user")
    private Integer idUser;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @PrePersist
    void initializeDate() {
        if (date == null) date = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public PropositionEmbauche getPropositionEmbauche() { return propositionEmbauche; }
    public void setPropositionEmbauche(PropositionEmbauche p) { this.propositionEmbauche = p; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public Integer getIdUser() { return idUser; }
    public void setIdUser(Integer idUser) { this.idUser = idUser; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
