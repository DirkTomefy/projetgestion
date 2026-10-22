package s5.itu.projetgestion.model.recrutement;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "traitement_detail")
public class Evaluation {

    public static final String DECISION_ADMIS = "ADMIS";
    public static final String DECISION_NON_ADMIS = "NON_ADMIS";
    public static final String DECISION_ABSENT = "ABSENT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_traitement", nullable = false)
    private Traitement traitement;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_workflow_traitement", nullable = false)
    private NiveauSelection niveau;

    @Column(name = "date_prevue")
    private LocalDate datePrevue;

    @Column(name = "date_realisee")
    private LocalDate dateRealisee;

    /** Précision libre : lieu, heure, sujet du test… */
    @Column(length = 255)
    private String libelle;

    /** Poste qui fait passer l'évaluation (son titulaire la voit dans « Mes évaluations »). */
    @Column(name = "id_poste_responsable")
    private Integer idPosteResponsable;

    /** Utilisateur qui a saisi le résultat. */
    @Column(name = "id_user")
    private Integer idUser;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(length = 20)
    private String decision;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    public boolean estFaite() { return decision != null; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Traitement getTraitement() { return traitement; }
    public void setTraitement(Traitement traitement) { this.traitement = traitement; }
    public NiveauSelection getNiveau() { return niveau; }
    public void setNiveau(NiveauSelection niveau) { this.niveau = niveau; }
    public LocalDate getDatePrevue() { return datePrevue; }
    public void setDatePrevue(LocalDate datePrevue) { this.datePrevue = datePrevue; }
    public LocalDate getDateRealisee() { return dateRealisee; }
    public void setDateRealisee(LocalDate dateRealisee) { this.dateRealisee = dateRealisee; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public Integer getIdPosteResponsable() { return idPosteResponsable; }
    public void setIdPosteResponsable(Integer idPosteResponsable) { this.idPosteResponsable = idPosteResponsable; }
    public Integer getIdUser() { return idUser; }
    public void setIdUser(Integer idUser) { this.idUser = idUser; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
