package s5.itu.projetgestion.model.organisation;

import jakarta.persistence.*;

@Entity
@Table(name = "metier")
public class Metier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, length = 150)
    private String libelle;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categorie", nullable = false)
    private CategorieMetier categorie;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String value) {
        libelle = value;
    }

    public CategorieMetier getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieMetier value) {
        categorie = value;
    }
}
