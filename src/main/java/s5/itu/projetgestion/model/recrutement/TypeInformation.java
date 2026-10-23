package s5.itu.projetgestion.model.recrutement;

import jakarta.persistence.*;

@Entity
@Table(name = "type_information")
public class TypeInformation {
    public static final String EXPERIENCE_PRO = "EXPERIENCE PRO";
    public static final String EDUCATION = "EDUCATION";
    public static final String QUALITE = "QUALITE";
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true, length = 30)
    private String libelle;
    public Integer getId() { return id; } public void setId(Integer id) { this.id = id; }
    public String getLibelle() { return libelle; } public void setLibelle(String libelle) { this.libelle = libelle; }
}
