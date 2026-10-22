package s5.itu.projetgestion.model.recrutement;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "histo_candidature")
public class HistoCandidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidature", nullable = false)
    private Candidature candidature;

    @Column(name = "etat_avant", length = 20)
    private String etatAvant;

    @Column(name = "etat_apres", nullable = false, length = 20)
    private String etatApres;

    /** NULL : dépôt depuis le site public ou décision automatique. */
    @Column(name = "id_user")
    private Integer idUser;

    @Column(name = "date_changement", nullable = false)
    private LocalDateTime dateChangement;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Candidature getCandidature() { return candidature; }
    public void setCandidature(Candidature candidature) { this.candidature = candidature; }
    public String getEtatAvant() { return etatAvant; }
    public void setEtatAvant(String etatAvant) { this.etatAvant = etatAvant; }
    public String getEtatApres() { return etatApres; }
    public void setEtatApres(String etatApres) { this.etatApres = etatApres; }
    public Integer getIdUser() { return idUser; }
    public void setIdUser(Integer idUser) { this.idUser = idUser; }
    public LocalDateTime getDateChangement() { return dateChangement; }
    public void setDateChangement(LocalDateTime dateChangement) { this.dateChangement = dateChangement; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
