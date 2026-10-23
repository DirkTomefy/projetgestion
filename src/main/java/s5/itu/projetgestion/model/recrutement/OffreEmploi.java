package s5.itu.projetgestion.model.recrutement;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.commun.Document;
import s5.itu.projetgestion.model.contrat.TypeContrat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "offre_emploi")
public class OffreEmploi {

    public static final String ETAT_BROUILLON = "BROUILLON";
    public static final String ETAT_PUBLIEE   = "PUBLIEE";
    public static final String ETAT_CLOTUREE  = "CLOTUREE";

    /** Motifs de clôture (P4, sprint 2). */
    public static final String CLOTURE_DATE_LIMITE    = "DATE_LIMITE";
    public static final String CLOTURE_POSTES_POURVUS = "POSTES_POURVUS";
    public static final String CLOTURE_MANUELLE       = "MANUELLE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_besoin", nullable = false)
    private Integer idBesoin;

    /** Poste du besoin couvert par l'offre ; NULL pour une offre antérieure qui couvre tout le besoin. */
    @Column(name = "id_poste")
    private Integer idPoste;

    /** Fiche de poste dont l'offre reprend les missions et le profil (NULL si aucune). */
    @Column(name = "id_fiche_poste")
    private Integer idFichePoste;

    @Column(name = "ref", nullable = false, unique = true, length = 50)
    private String ref;

    /** Facultatif (➕ cahier des charges). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_type_contrat")
    private TypeContrat typeContrat;

    @Column(name = "date_publication")
    private LocalDate datePublication;

    @Column(name = "date_limite_candidature")
    private LocalDate dateLimiteCandidature;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_document")
    private Document document;

    @Column(nullable = false, length = 20)
    private String etat = ETAT_BROUILLON;

    @Column(name = "date_cloture")
    private LocalDateTime dateCloture;

    @Column(name = "motif_cloture", length = 20)
    private String motifCloture;

    /** NULL pour une clôture automatique. */
    @Column(name = "id_user_cloture")
    private Integer idUserCloture;

    public Integer getIdFichePoste() { return idFichePoste; }
    public void setIdFichePoste(Integer idFichePoste) { this.idFichePoste = idFichePoste; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdBesoin() { return idBesoin; }
    public void setIdBesoin(Integer idBesoin) { this.idBesoin = idBesoin; }
    public Integer getIdPoste() { return idPoste; }
    public void setIdPoste(Integer idPoste) { this.idPoste = idPoste; }
    public String getRef() { return ref; }
    public void setRef(String ref) { this.ref = ref; }
    public TypeContrat getTypeContrat() { return typeContrat; }
    public void setTypeContrat(TypeContrat typeContrat) { this.typeContrat = typeContrat; }
    public LocalDate getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDate datePublication) { this.datePublication = datePublication; }
    public LocalDate getDateLimiteCandidature() { return dateLimiteCandidature; }
    public void setDateLimiteCandidature(LocalDate d) { this.dateLimiteCandidature = d; }
    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
    public LocalDateTime getDateCloture() { return dateCloture; }
    public void setDateCloture(LocalDateTime dateCloture) { this.dateCloture = dateCloture; }
    public String getMotifCloture() { return motifCloture; }
    public void setMotifCloture(String motifCloture) { this.motifCloture = motifCloture; }
    public Integer getIdUserCloture() { return idUserCloture; }
    public void setIdUserCloture(Integer idUserCloture) { this.idUserCloture = idUserCloture; }
}
