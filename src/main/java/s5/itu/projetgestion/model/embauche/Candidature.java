package s5.itu.projetgestion.model.embauche;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.personnel.Candidat;
import s5.itu.projetgestion.model.rh.OffreEmploi;

import java.time.LocalDate;

@Entity
@Table(name = "candidatures", uniqueConstraints = @UniqueConstraint(columnNames = { "id_candidat", "id_offre_emploi" }))
public class Candidature {
    public static final String ETAT_DEPOSEE = "DEPOSEE";
    public static final String ETAT_PRESELECTIONNER = "PRESELECTIONNER";
    public static final String ETAT_REFUSE = "REFUSE";
    public static final String ETAT_DESISTE = "DESISTE";
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "id_candidat", nullable = false)
    private Candidat candidat;
    @Column(name = "id_offre_emploi", nullable = false)
    private Integer idOffreEmploi;
    @Column(name = "date_depot", nullable = false)
    private LocalDate dateDepot;
    @Column(nullable = false, length = 20)
    private String etat = ETAT_DEPOSEE;
    public Integer getId() { return id; } public void setId(Integer id) { this.id = id; }
    public Candidat getCandidat() { return candidat; } public void setCandidat(Candidat value) { candidat = value; }
    public Integer getIdOffreEmploi() { return idOffreEmploi; } public void setIdOffreEmploi(Integer value) { idOffreEmploi = value; }
    public LocalDate getDateDepot() { return dateDepot; } public void setDateDepot(LocalDate value) { dateDepot = value; }
    public String getEtat() { return etat; } public void setEtat(String value) { etat = value; }
}
