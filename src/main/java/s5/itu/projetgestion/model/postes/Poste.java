package s5.itu.projetgestion.model.postes;

import jakarta.persistence.*;

@Entity
@Table(name = "poste")
public class Poste {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_metier", nullable = false)
    private Metier metier;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_echelon", nullable = false)
    private EchelonMetier echelon;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_unite_orga", nullable = false)
    private UniteOrga uniteOrga;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_superieur_direct")
    private Poste superieurDirect;
    @Column(nullable = false, length = 150)
    private String libelle;
    @Column(nullable = false, length = 20)
    private String etat;
    @Column(name = "statut_occupation", nullable = false, length = 20)
    private String statutOccupation;

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

    public Metier getMetier() {
        return metier;
    }

    public void setMetier(Metier value) {
        metier = value;
    }

    public EchelonMetier getEchelon() {
        return echelon;
    }

    public void setEchelon(EchelonMetier value) {
        echelon = value;
    }

    public UniteOrga getUniteOrga() {
        return uniteOrga;
    }

    public void setUniteOrga(UniteOrga value) {
        uniteOrga = value;
    }

    public Poste getSuperieurDirect() {
        return superieurDirect;
    }

    public void setSuperieurDirect(Poste value) {
        superieurDirect = value;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String value) {
        libelle = value;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String value) {
        etat = value;
    }

    public String getStatutOccupation() {
        return statutOccupation;
    }

    public void setStatutOccupation(String value) {
        statutOccupation = value;
    }
}
