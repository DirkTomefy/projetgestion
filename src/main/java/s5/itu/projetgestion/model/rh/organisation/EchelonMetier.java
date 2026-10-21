package s5.itu.projetgestion.model.rh.organisation;

import jakarta.persistence.*;

@Entity
@Table(name = "echelon_metier")
public class EchelonMetier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String libelle;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}