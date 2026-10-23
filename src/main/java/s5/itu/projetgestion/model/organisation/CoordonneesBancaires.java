package s5.itu.projetgestion.model.organisation;

import java.time.LocalDate;

import jakarta.persistence.*;

/** Coordonnées bancaires d'un employé (paie). Une seule ligne active à la fois ; l'historique est conservé. */
@Entity
@Table(name = "coordonnees_bancaires")
public class CoordonneesBancaires {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_employe", nullable = false)
    private Employe employe;
    @Column(nullable = false, length = 150)
    private String banque;
    @Column(length = 150)
    private String agence;
    @Column(nullable = false, length = 200)
    private String titulaire;
    @Column(nullable = false, length = 50)
    private String rib;
    @Column(name = "date_effet", nullable = false)
    private LocalDate dateEffet = LocalDate.now();
    @Column(nullable = false)
    private boolean actif = true;

    public Integer getId() { return id; }
    public Employe getEmploye() { return employe; }
    public void setEmploye(Employe employe) { this.employe = employe; }
    public String getBanque() { return banque; }
    public void setBanque(String banque) { this.banque = banque; }
    public String getAgence() { return agence; }
    public void setAgence(String agence) { this.agence = agence; }
    public String getTitulaire() { return titulaire; }
    public void setTitulaire(String titulaire) { this.titulaire = titulaire; }
    public String getRib() { return rib; }
    public void setRib(String rib) { this.rib = rib; }
    public LocalDate getDateEffet() { return dateEffet; }
    public void setDateEffet(LocalDate dateEffet) { this.dateEffet = dateEffet; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
