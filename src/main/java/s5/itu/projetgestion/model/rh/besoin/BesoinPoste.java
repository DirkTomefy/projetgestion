package s5.itu.projetgestion.model.rh.besoin;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.rh.organisation.Poste;

@Entity
@Table(name = "besoin_poste")
public class BesoinPoste {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_besoin")
    private Besoin besoin;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_poste")
    private Poste poste;

    @Column(nullable = false, length = 20)
    private String etat = "EN_ATTENTE";

    // utilisé plus tard par les autres : simple Integer pour l'instant
    @Column(name = "id_candidature_retenue")
    private Integer idCandidatureRetenue;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Besoin getBesoin() { return besoin; }
    public void setBesoin(Besoin besoin) { this.besoin = besoin; }
    public Poste getPoste() { return poste; }
    public void setPoste(Poste poste) { this.poste = poste; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
    public Integer getIdCandidatureRetenue() { return idCandidatureRetenue; }
    public void setIdCandidatureRetenue(Integer idCandidatureRetenue) { this.idCandidatureRetenue = idCandidatureRetenue; }
}
