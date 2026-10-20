package s5.itu.projetgestion.model.validation;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.postes.Poste;

@Entity
@Table(name = "workflow_validation", uniqueConstraints = @UniqueConstraint(columnNames = { "id_poste", "ordre" }))
public class WorkflowValidation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_poste", nullable = false)
    private Poste poste;
    @Column(nullable = false)
    private Integer ordre;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Poste getPoste() {
        return poste;
    }

    public void setPoste(Poste value) {
        poste = value;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer value) {
        ordre = value;
    }
}
