package s5.itu.projetgestion.model.besoin;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.organisation.Employe;
import s5.itu.projetgestion.model.organisation.Poste;

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
    @Column(name = "id_candidature_retenue")
    private Integer idCandidatureRetenue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe_remplace")
    private Employe employeRemplace;
    // + getter et setter

  

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

    public Integer getIdCandidatureRetenue() {
        return idCandidatureRetenue;
    }

    public void setIdCandidatureRetenue(Integer value) {
        idCandidatureRetenue = value;
    }

      public  Employe getEmployeRemplace() {
        return  employeRemplace ;
    }

    public  void  setEmployeRemplace(Employe employeRemplace){
        this.employeRemplace = employeRemplace ;
    }
}
