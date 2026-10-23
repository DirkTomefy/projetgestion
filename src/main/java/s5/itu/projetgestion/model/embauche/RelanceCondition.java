package s5.itu.projetgestion.model.embauche;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Contre-proposition de la RH sur une condition refusée par le candidat. */
@Entity
@Table(name = "relance_condition")
public class RelanceCondition {

    public static final String STATUT_EN_ATTENTE = "EN_ATTENTE";
    public static final String STATUT_ACCEPTEE = "ACCEPTEE";
    public static final String STATUT_REFUSEE = "REFUSEE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proposition_condition", nullable = false)
    private PropositionCondition propositionCondition;

    @Column(name = "ancienne_valeur", columnDefinition = "TEXT")
    private String ancienneValeur;

    @Column(name = "nouvelle_valeur", columnDefinition = "TEXT")
    private String nouvelleValeur;

    @Column(length = 30)
    private String statut = STATUT_EN_ATTENTE;

    @Column(name = "id_user")
    private Integer idUser;

    @Column(name = "date_relance", nullable = false)
    private LocalDateTime dateRelance;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @PrePersist
    void initialiserDate() {
        if (dateRelance == null) dateRelance = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public PropositionCondition getPropositionCondition() { return propositionCondition; }
    public void setPropositionCondition(PropositionCondition c) { this.propositionCondition = c; }
    public String getAncienneValeur() { return ancienneValeur; }
    public void setAncienneValeur(String ancienneValeur) { this.ancienneValeur = ancienneValeur; }
    public String getNouvelleValeur() { return nouvelleValeur; }
    public void setNouvelleValeur(String nouvelleValeur) { this.nouvelleValeur = nouvelleValeur; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public Integer getIdUser() { return idUser; }
    public void setIdUser(Integer idUser) { this.idUser = idUser; }
    public LocalDateTime getDateRelance() { return dateRelance; }
    public void setDateRelance(LocalDateTime dateRelance) { this.dateRelance = dateRelance; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
