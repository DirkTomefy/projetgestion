package s5.itu.projetgestion.model.postes;

import jakarta.persistence.*;

@Entity
@Table(name = "employe")
public class Employe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true, length = 30)
    private String matricule;
    @Column(nullable = false, length = 100)
    private String nom;
    @Column(length = 150)
    private String prenoms;
    @Column(length = 255)
    private String adresse;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String value) {
        matricule = value;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String value) {
        nom = value;
    }

    public String getPrenoms() {
        return prenoms;
    }

    public void setPrenoms(String value) {
        prenoms = value;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String value) {
        adresse = value;
    }
}
