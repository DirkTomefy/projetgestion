package s5.itu.projetgestion.model.rh;

import jakarta.persistence.*;

@Entity
@Table(name = "type_contrat")
public class TypeContrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String libelle;

    @Column(name = "duree_max")
    private Integer dureeMax;

    @Column(name = "renouvellement_max")
    private Integer renouvellementMax;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public Integer getDureeMax() { return dureeMax; }
    public void setDureeMax(Integer dureeMax) { this.dureeMax = dureeMax; }
    public Integer getRenouvellementMax() { return renouvellementMax; }
    public void setRenouvellementMax(Integer renouvellementMax) { this.renouvellementMax = renouvellementMax; }
}
