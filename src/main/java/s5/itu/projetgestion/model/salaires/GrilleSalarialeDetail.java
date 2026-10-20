package s5.itu.projetgestion.model.salaires;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.postes.CategorieMetier;
import s5.itu.projetgestion.model.postes.EchelonMetier;

@Entity
@Table(name = "grille_salariale_details", uniqueConstraints = @UniqueConstraint(columnNames = { "id_grille",
        "id_categorie", "id_echelon" }))
public class GrilleSalarialeDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_grille", nullable = false)
    private GrilleSalariale grille;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_categorie", nullable = false)
    private CategorieMetier categorie;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_echelon", nullable = false)
    private EchelonMetier echelon;
    @Column(name = "montant_min", nullable = false)
    private Double montantMin;
    @Column(name = "montant_max", nullable = false)
    private Double montantMax;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public GrilleSalariale getGrille() {
        return grille;
    }

    public void setGrille(GrilleSalariale value) {
        grille = value;
    }

    public CategorieMetier getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieMetier value) {
        categorie = value;
    }

    public EchelonMetier getEchelon() {
        return echelon;
    }

    public void setEchelon(EchelonMetier value) {
        echelon = value;
    }

    public Double getMontantMin() {
        return montantMin;
    }

    public void setMontantMin(Double value) {
        montantMin = value;
    }

    public Double getMontantMax() {
        return montantMax;
    }

    public void setMontantMax(Double value) {
        montantMax = value;
    }
}
