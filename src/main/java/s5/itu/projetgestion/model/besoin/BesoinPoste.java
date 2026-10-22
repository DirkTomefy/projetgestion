package s5.itu.projetgestion.model.besoin;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.organisation.Poste;
import s5.itu.projetgestion.model.recrutement.Candidature;
@Entity
@Table(name = "besoin_poste")
public class BesoinPoste {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_besoin", nullable = false)
    private Besoin besoin;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_poste", nullable = false)
    private Poste poste;
    @Column(nullable = false, length = 20)
    private String etat;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_candidature_retenue")
    private Candidature candidatureRetenue;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Besoin getBesoin() {
        return besoin;
    }

    public void setBesoin(Besoin value) {
        besoin = value;
    }

    public Poste getPoste() {
        return poste;
    }

    public void setPoste(Poste value) {
        poste = value;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String value) {
        etat = value;
    }

    public Candidature getCandidatureRetenue() {
        return candidatureRetenue;
    }

    public void setCandidatureRetenue(Candidature value) {
        candidatureRetenue = value;
    }
}
