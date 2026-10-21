package s5.itu.projetgestion.model.finance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import s5.itu.projetgestion.model.organisation.UniteOrga;

@Entity
@Table(name = "budget_unite")
public class BudgetUnite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_unite_orga", nullable = false)
    private UniteOrga uniteOrga;

    @Column 
    private Integer exercice;

    @Column 
    private Double montant;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UniteOrga getUniteOrga() {
        return uniteOrga;
    }

    public void setUniteOrga(UniteOrga uniteOrga) {
        this.uniteOrga = uniteOrga;
    }

    public Integer getExercice() {
        return exercice;
    }

    public void setExercice(Integer exercice) {
        this.exercice = exercice;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    
    
}
