package s5.itu.projetgestion.model.besoin;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.auth.User;

/** Historique des états d'un besoin : création, validations, refus, annulation, pourvu. */
@Entity
@Table(name = "histo_besoin")
public class HistoBesoin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_besoin", nullable = false)
    private Besoin besoin;
    @Column(name = "etat_avant", length = 20)
    private String etatAvant;
    @Column(name = "etat_apres", nullable = false, length = 20)
    private String etatApres;
    @Column(nullable = false, length = 50)
    private String action;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private User user;
    @Column(name = "date_action", nullable = false)
    private LocalDateTime dateAction = LocalDateTime.now();
    @Column(columnDefinition = "TEXT")
    private String commentaire;

    public Integer getId() { return id; }
    public Besoin getBesoin() { return besoin; }
    public void setBesoin(Besoin besoin) { this.besoin = besoin; }
    public String getEtatAvant() { return etatAvant; }
    public void setEtatAvant(String etatAvant) { this.etatAvant = etatAvant; }
    public String getEtatApres() { return etatApres; }
    public void setEtatApres(String etatApres) { this.etatApres = etatApres; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getDateAction() { return dateAction; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
