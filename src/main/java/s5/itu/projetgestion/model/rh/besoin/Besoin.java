package s5.itu.projetgestion.model.rh.besoin;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.User;
import s5.itu.projetgestion.model.rh.organisation.UniteOrga;

@Entity
@Table(name = "besoin")
public class Besoin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(name = "type_besoin", nullable = false, length = 50)
    private String typeBesoin;          // CREATION ou REMPLACEMENT

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_motif")
    private Motif motif;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_unite_orga")
    private UniteOrga uniteOrga;

    @Column(name = "date_souhaitee")
    private LocalDate dateSouhaitee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_user")
    private User user;

    @Column(nullable = false)
    private Integer effectif;

    @Column(name = "etat_actuel", nullable = false, length = 20)
    private String etatActuel = "EN_ATTENTE";

    @Column(name = "budget_estime", precision = 15, scale = 2)
    private BigDecimal budgetEstime;    // rempli par P3

    @Column(name = "precision_motif", length = 100)
    private String precisionMotif;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getTypeBesoin() { return typeBesoin; }
    public void setTypeBesoin(String typeBesoin) { this.typeBesoin = typeBesoin; }
    public Motif getMotif() { return motif; }
    public void setMotif(Motif motif) { this.motif = motif; }
    public UniteOrga getUniteOrga() { return uniteOrga; }
    public void setUniteOrga(UniteOrga unite) { this.uniteOrga = unite; }
    public LocalDate getDateSouhaitee() { return dateSouhaitee; }
    public void setDateSouhaitee(LocalDate dateSouhaitee) { this.dateSouhaitee = dateSouhaitee; }
    public User getUser() { return user; }
    public void setUser(User user2) { this.user = user2; }
    public Integer getEffectif() { return effectif; }
    public void setEffectif(Integer effectif) { this.effectif = effectif; }
    public String getEtatActuel() { return etatActuel; }
    public void setEtatActuel(String etatActuel) { this.etatActuel = etatActuel; }
    public BigDecimal getBudgetEstime() { return budgetEstime; }
    public void setBudgetEstime(BigDecimal budgetEstime) { this.budgetEstime = budgetEstime; }
    public String getPrecisionMotif() { return precisionMotif; }
    public void setPrecisionMotif(String precisionMotif) { this.precisionMotif = precisionMotif; }
}