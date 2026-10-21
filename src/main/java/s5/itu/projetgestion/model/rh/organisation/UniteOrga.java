package s5.itu.projetgestion.model.rh.organisation;

import jakarta.persistence.*;

@Entity
@Table(name = "unite_orga")
public class UniteOrga {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 20)
    private String type;   // DIRECTION, DEPARTEMENT, SERVICE, SECTION

    @Column(nullable = false, length = 150)
    private String libelle;

    @ManyToOne
    @JoinColumn(name = "id_unite_mere")
    private UniteOrga uniteMere;

    // simple Integer pour éviter une boucle UniteOrga <-> Poste
    @Column(name = "id_poste_responsable")
    private Integer idPosteResponsable;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public UniteOrga getUniteMere() { return uniteMere; }
    public void setUniteMere(UniteOrga uniteMere) { this.uniteMere = uniteMere; }
    public Integer getIdPosteResponsable() { return idPosteResponsable; }
    public void setIdPosteResponsable(Integer idPosteResponsable) { this.idPosteResponsable = idPosteResponsable; }
}