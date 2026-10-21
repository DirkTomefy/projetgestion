package s5.itu.projetgestion.model.postes;

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
    private String type;
    @Column(nullable = false, length = 150)
    private String libelle;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unite_mere")
    private UniteOrga uniteMere;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_poste_responsable")
    private Poste posteResponsable;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String value) {
        code = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        type = value;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String value) {
        libelle = value;
    }

    public UniteOrga getUniteMere() {
        return uniteMere;
    }

    public void setUniteMere(UniteOrga value) {
        uniteMere = value;
    }

    public Poste getPosteResponsable() {
        return posteResponsable;
    }

    public void setPosteResponsable(Poste value) {
        posteResponsable = value;
    }
}
