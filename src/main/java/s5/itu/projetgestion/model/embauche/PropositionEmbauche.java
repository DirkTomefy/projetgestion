package s5.itu.projetgestion.model.embauche;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.besoin.BesoinPoste;
import s5.itu.projetgestion.model.recrutement.Candidature;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "proposition_embauche")
public class PropositionEmbauche {

    /** En attente : réponse du candidat, ou contre-proposition de la RH après un refus partiel. */
    public static final String STATUT_PROPOSER = "PROPOSER";
    /** Refus définitif (candidat qui décline, ou RH qui abandonne après un refus). */
    public static final String STATUT_REFUSER = "REFUSER";
    /** Toutes les conditions acceptées : l'embauche (P3) est lancée. */
    public static final String STATUT_ACCEPTER = "ACCEPTER";
    /** Date limite dépassée sans réponse du candidat. */
    public static final String STATUT_EXPIREE = "EXPIREE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidature", nullable = false)
    private Candidature candidature;

    @Column(name = "id_statut_actuel", nullable = false, length = 20)
    private String statutActuel = STATUT_PROPOSER;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_besoin_poste", nullable = false)
    private BesoinPoste besoinPoste;

    @Column(name = "date_limite_reponse")
    private LocalDate dateLimiteReponse;

    /** Jeton aléatoire du lien personnel envoyé au candidat (page publique de réponse). */
    @Column(nullable = false, unique = true, length = 64)
    private String jeton;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;

    @Column(name = "date_reponse")
    private LocalDateTime dateReponse;

    public boolean estEnCours() {
        return STATUT_PROPOSER.equals(statutActuel);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Candidature getCandidature() { return candidature; }
    public void setCandidature(Candidature candidature) { this.candidature = candidature; }
    public String getStatutActuel() { return statutActuel; }
    public void setStatutActuel(String statutActuel) { this.statutActuel = statutActuel; }
    public BesoinPoste getBesoinPoste() { return besoinPoste; }
    public void setBesoinPoste(BesoinPoste besoinPoste) { this.besoinPoste = besoinPoste; }
    public LocalDate getDateLimiteReponse() { return dateLimiteReponse; }
    public void setDateLimiteReponse(LocalDate dateLimiteReponse) { this.dateLimiteReponse = dateLimiteReponse; }
    public String getJeton() { return jeton; }
    public void setJeton(String jeton) { this.jeton = jeton; }
    public LocalDateTime getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDateTime dateEnvoi) { this.dateEnvoi = dateEnvoi; }
    public LocalDateTime getDateReponse() { return dateReponse; }
    public void setDateReponse(LocalDateTime dateReponse) { this.dateReponse = dateReponse; }
}
