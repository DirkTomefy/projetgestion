package s5.itu.projetgestion.model.besoin;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.organisation.Poste;

@Entity
@Table(name = "workflow_validation", uniqueConstraints = @UniqueConstraint(columnNames = { "id_poste", "ordre" }))
public class WorkflowValidation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_poste", nullable = false)
    private Poste poste;
    @Column(nullable = false)
    private Integer ordre;

    /** Contrôle automatique de l'étape : BUDGET (validée d'office si le coût tient dans le budget), ou null. */
    @Column(length = 20)
    private String controle;

    public String getControle() { return controle; }
    public void setControle(String controle) { this.controle = controle; }

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
