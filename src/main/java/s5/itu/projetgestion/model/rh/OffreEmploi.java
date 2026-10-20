package s5.itu.projetgestion.model.rh;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "offre_emploi")
public class OffreEmploi {

    public static final String ETAT_BROUILLON = "BROUILLON";
    public static final String ETAT_PUBLIEE   = "PUBLIEE";
    public static final String ETAT_CLOTUREE  = "CLOTUREE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_besoin", nullable = false)
    private Integer idBesoin;

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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdBesoin() { return idBesoin; }
    public void setIdBesoin(Integer idBesoin) { this.idBesoin = idBesoin; }
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
}
