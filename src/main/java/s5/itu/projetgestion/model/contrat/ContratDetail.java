package s5.itu.projetgestion.model.contrat;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.recrutement.PointCondition;

@Entity
@Table(name = "contrat_details")
public class ContratDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrat", nullable = false)
    private Contrat contrat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_point_condition", nullable = false)
    private PointCondition pointCondition;

    @Column(columnDefinition = "TEXT")
    private String valeur;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Contrat getContrat() { return contrat; }
    public void setContrat(Contrat contrat) { this.contrat = contrat; }
    public PointCondition getPointCondition() { return pointCondition; }
    public void setPointCondition(PointCondition pointCondition) { this.pointCondition = pointCondition; }
    public String getValeur() { return valeur; }
    public void setValeur(String valeur) { this.valeur = valeur; }
}
