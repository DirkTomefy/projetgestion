package s5.itu.projetgestion.model.personnel;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "candidat_detail")
public class CandidatDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "id_candidat", nullable = false)
    private Candidat candidat;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "id_type_information", nullable = false)
    private TypeInformation typeInformation;
    @Column(nullable = false, length = 255)
    private String libelle;
    @Column(name = "date_debut") private LocalDate dateDebut;
    @Column(name = "date_fin") private LocalDate dateFin;
    public boolean estEquivalente(Integer idType, String autreLibelle, LocalDate debut, LocalDate fin) {
        return typeInformation != null && Objects.equals(typeInformation.getId(), idType)
                && normaliser(libelle).equals(normaliser(autreLibelle))
                && Objects.equals(dateDebut, debut) && Objects.equals(dateFin, fin);
    }
    private static String normaliser(String s) { return s == null ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT); }
    public Integer getId() { return id; } public void setId(Integer id) { this.id = id; }
    public Candidat getCandidat() { return candidat; } public void setCandidat(Candidat candidat) { this.candidat = candidat; }
    public TypeInformation getTypeInformation() { return typeInformation; } public void setTypeInformation(TypeInformation value) { typeInformation = value; }
    public String getLibelle() { return libelle; } public void setLibelle(String libelle) { this.libelle = libelle; }
    public LocalDate getDateDebut() { return dateDebut; } public void setDateDebut(LocalDate value) { dateDebut = value; }
    public LocalDate getDateFin() { return dateFin; } public void setDateFin(LocalDate value) { dateFin = value; }
}
