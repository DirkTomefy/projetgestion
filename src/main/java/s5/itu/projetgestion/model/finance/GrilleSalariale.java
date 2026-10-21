package s5.itu.projetgestion.model.finance;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "grille_salariale")
public class GrilleSalariale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "date_effectif", nullable = false)
    private LocalDate dateEffectif;
    @Column(name = "date_fin")
    private LocalDate dateFin;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDateEffectif() {
        return dateEffectif;
    }

    public void setDateEffectif(LocalDate value) {
        dateEffectif = value;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate value) {
        dateFin = value;
    }
}
