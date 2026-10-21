package s5.itu.projetgestion.model.besoin;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.User;
import s5.itu.projetgestion.model.postes.UniteOrga;

import java.time.LocalDate;

@Entity
@Table(name = "besoin")
public class Besoin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(name = "type_besoin", nullable = false, length = 50)
    private String typeBesoin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_motif", nullable = false)
    private Motif motif;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_unite_orga", nullable = false)
    private UniteOrga uniteOrga;

    private LocalDate dateSouhaitee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer effectif;

    @Column(name = "etat_actuel", nullable = false, length = 20)
    private String etatActuel;

    @Column
    private Double budgetEstime;

    @Column(name = "precision_motif", length = 100)
    private String precisionMotif;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String value) {
        reference = value;
    }

    public String getTypeBesoin() {
        return typeBesoin;
    }

    public void setTypeBesoin(String value) {
        typeBesoin = value;
    }

    public Motif getMotif() {
        return motif;
    }

    public void setMotif(Motif value) {
        motif = value;
    }

    public UniteOrga getUniteOrga() {
        return uniteOrga;
    }

    public void setUniteOrga(UniteOrga value) {
        uniteOrga = value;
    }

    public LocalDate getDateSouhaitee() {
        return dateSouhaitee;
    }

    public void setDateSouhaitee(LocalDate value) {
        dateSouhaitee = value;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User value) {
        user = value;
    }

    public Integer getEffectif() {
        return effectif;
    }

    public void setEffectif(Integer value) {
        effectif = value;
    }

    public String getEtatActuel() {
        return etatActuel;
    }

    public void setEtatActuel(String value) {
        etatActuel = value;
    }

    public Double getBudgetEstime() {
        return budgetEstime;
    }

    public void setBudgetEstime(Double value) {
        budgetEstime = value;
    }

    public String getPrecisionMotif() {
        return precisionMotif;
    }

    public void setPrecisionMotif(String value) {
        precisionMotif = value;
    }
}
