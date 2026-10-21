package s5.itu.projetgestion.model.rh.besoin;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.rh.organisation.Poste;

@Entity
@Table(name = "workflow_validation")
public class WorkflowValidation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_poste")
    private Poste poste;        // poste validateur

    @Column(nullable = false)
    private Integer ordre;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Poste getPoste() { return poste; }
    public void setPoste(Poste poste) { this.poste = poste; }
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
}