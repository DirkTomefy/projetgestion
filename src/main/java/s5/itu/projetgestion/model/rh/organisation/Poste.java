package s5.itu.projetgestion.model.rh.organisation;

import jakarta.persistence.*;

@Entity
@Table(name = "poste")
public class Poste {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_metier")
    private Metier metier;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_echelon")
    private EchelonMetier echelon;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_unite_orga")
    private UniteOrga uniteOrga;

    @ManyToOne
    @JoinColumn(name = "id_superieur_direct")
    private Poste superieurDirect;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(nullable = false, length = 20)
    private String etat = "PROPOSE";              // PROPOSE, ACTIF, REFUSE, SUPPRIME

    @Column(name = "statut_occupation", nullable = false, length = 20)
    private String statutOccupation = "LIBRE";    // LIBRE, OCCUPE

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Metier getMetier() { return metier; }
    public void setMetier(Metier metier) { this.metier = metier; }
    public EchelonMetier getEchelon() { return echelon; }
    public void setEchelon(EchelonMetier echelon) { this.echelon = echelon; }
    public UniteOrga getUniteOrga() { return uniteOrga; }
    public void setUniteOrga(UniteOrga uniteOrga) { this.uniteOrga = uniteOrga; }
    public Poste getSuperieurDirect() { return superieurDirect; }
    public void setSuperieurDirect(Poste superieurDirect) { this.superieurDirect = superieurDirect; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
    public String getStatutOccupation() { return statutOccupation; }
    public void setStatutOccupation(String statutOccupation) { this.statutOccupation = statutOccupation; }
}