package s5.itu.projetgestion.model.embauche;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.personnel.Candidat;
import s5.itu.projetgestion.model.rh.OffreEmploi;

import java.time.LocalDate;

@Entity
@Table(name = "candidatures", uniqueConstraints = @UniqueConstraint(columnNames = { "id_candidat", "id_offre_emploi" }))
public class Candidature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidat", nullable = false)
    private Candidat candidat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_offre_emploi", nullable = false)
    private OffreEmploi offreEmploi;

    @Column(name = "date_depot", nullable = false)
    private LocalDate dateDepot;

    @Column(nullable = false, length = 20)
    private String etat;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Candidat getCandidat() { return candidat; }
    public void setCandidat(Candidat candidat) { this.candidat = candidat; }
    public OffreEmploi getOffreEmploi() { return offreEmploi; }
    public void setOffreEmploi(OffreEmploi offreEmploi) { this.offreEmploi = offreEmploi; }
    public LocalDate getDateDepot() { return dateDepot; }
    public void setDateDepot(LocalDate dateDepot) { this.dateDepot = dateDepot; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
}
