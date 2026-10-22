package s5.itu.projetgestion.model.embauche;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.besoin.BesoinPoste;

import java.time.LocalDate;

@Entity
@Table(name = "proposition_embauche")
public class PropositionEmbauche {
    public static final String STATUT_PROPOSER = "PROPOSER";
    public static final String STATUT_REFUSER = "REFUSER";
    public static final String STATUT_ACCEPTER = "ACCEPTER";

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
}
