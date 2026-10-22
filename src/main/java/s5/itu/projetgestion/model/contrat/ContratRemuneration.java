package s5.itu.projetgestion.model.contrat;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.organisation.EchelonMetier;

import java.time.LocalDate;

@Entity
@Table(name = "contrat_remuneration")
public class ContratRemuneration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrat", nullable = false)
    private Contrat contrat;

    @Column(name = "date_effet", nullable = false)
    private LocalDate dateEffet;

    @Column(nullable = false)
    private Double salaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_echelon")
    private EchelonMetier echelon;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Contrat getContrat() { return contrat; }
    public void setContrat(Contrat contrat) { this.contrat = contrat; }
    public LocalDate getDateEffet() { return dateEffet; }
    public void setDateEffet(LocalDate dateEffet) { this.dateEffet = dateEffet; }
    public Double getSalaire() { return salaire; }
    public void setSalaire(Double salaire) { this.salaire = salaire; }
    public EchelonMetier getEchelon() { return echelon; }
    public void setEchelon(EchelonMetier echelon) { this.echelon = echelon; }
}
