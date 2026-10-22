package s5.itu.projetgestion.model.embauche;

import jakarta.persistence.*;

/** Point négociable d'une proposition : SALAIRE, DATE_DEBUT, HORAIRE, LIEU, CONDITIONS, TYPE_CONTRAT. */
@Entity
@Table(name = "point_condition")
public class PointCondition {

    public static final String SALAIRE = "SALAIRE";
    public static final String DATE_DEBUT = "DATE_DEBUT";
    public static final String HORAIRE = "HORAIRE";
    public static final String LIEU = "LIEU";
    public static final String CONDITIONS = "CONDITIONS";
    public static final String TYPE_CONTRAT = "TYPE_CONTRAT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String libelle;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
