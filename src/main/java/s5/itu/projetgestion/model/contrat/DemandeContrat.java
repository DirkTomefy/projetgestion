package s5.itu.projetgestion.model.contrat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

/** Demande de nouveau contrat ou d'avenant sur un contrat signé. */
@Entity
@Table(name = "demande_contrat")
public class DemandeContrat {
    public static final String A_INSTRUIRE = "A_INSTRUIRE";
    public static final String A_VALIDER_FINANCE = "A_VALIDER_FINANCE";
    public static final String A_VALIDER_DG = "A_VALIDER_DG";
    public static final String VALIDEE = "VALIDEE";
    public static final String REFUSEE = "REFUSEE";
    public static final String ANNULEE = "ANNULEE";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true, length = 30)
    private String reference;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "id_contrat")
    private Contrat contrat;
    @Column(nullable = false, length = 10)
    private String origine;
    @Column(name = "motif_type", nullable = false, length = 20)
    private String motifType;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_type_contrat")
    private TypeContrat typeContrat;
    @Column(name = "date_effet", nullable = false)
    private LocalDate dateEffet;
    @Column(name = "date_fin_prevue")
    private LocalDate dateFinPrevue;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal salaire;
    @Column(columnDefinition = "text")
    private String motif;
    @Column(nullable = false, length = 20)
    private String etat;
    @Column(name = "depassement_budget", nullable = false)
    private boolean depassementBudget;
    @Column(name = "id_demandeur", nullable = false)
    private Integer idDemandeur;
    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande = LocalDateTime.now();
    @Column(name = "id_rh") private Integer idRh;
    @Column(name = "date_rh") private LocalDateTime dateRh;
    @Column(name = "commentaire_rh", columnDefinition = "text") private String commentaireRh;
    @Column(name = "id_finance") private Integer idFinance;
    @Column(name = "date_finance") private LocalDateTime dateFinance;
    @Column(name = "commentaire_finance", columnDefinition = "text") private String commentaireFinance;
    @Column(name = "id_dg") private Integer idDg;
    @Column(name = "date_dg") private LocalDateTime dateDg;
    @Column(name = "commentaire_dg", columnDefinition = "text") private String commentaireDg;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "id_contrat_resultat")
    private Contrat contratResultat;

    public Integer getId() { return id; }
    public String getReference() { return reference; }
    public void setReference(String v) { reference = v; }
    public Contrat getContrat() { return contrat; }
    public void setContrat(Contrat v) { contrat = v; }
    public String getOrigine() { return origine; }
    public void setOrigine(String v) { origine = v; }
    public String getMotifType() { return motifType; }
    public void setMotifType(String v) { motifType = v; }
    public TypeContrat getTypeContrat() { return typeContrat; }
    public void setTypeContrat(TypeContrat v) { typeContrat = v; }
    public LocalDate getDateEffet() { return dateEffet; }
    public void setDateEffet(LocalDate v) { dateEffet = v; }
    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate v) { dateFinPrevue = v; }
    public BigDecimal getSalaire() { return salaire; }
    public void setSalaire(BigDecimal v) { salaire = v; }
    public String getMotif() { return motif; }
    public void setMotif(String v) { motif = v; }
    public String getEtat() { return etat; }
    public void setEtat(String v) { etat = v; }
    public boolean isDepassementBudget() { return depassementBudget; }
    public void setDepassementBudget(boolean v) { depassementBudget = v; }
    public Integer getIdDemandeur() { return idDemandeur; }
    public void setIdDemandeur(Integer v) { idDemandeur = v; }
    public LocalDateTime getDateDemande() { return dateDemande; }
    public Integer getIdRh() { return idRh; }
    public void setIdRh(Integer v) { idRh = v; }
    public LocalDateTime getDateRh() { return dateRh; }
    public void setDateRh(LocalDateTime v) { dateRh = v; }
    public String getCommentaireRh() { return commentaireRh; }
    public void setCommentaireRh(String v) { commentaireRh = v; }
    public Integer getIdFinance() { return idFinance; }
    public void setIdFinance(Integer v) { idFinance = v; }
    public LocalDateTime getDateFinance() { return dateFinance; }
    public void setDateFinance(LocalDateTime v) { dateFinance = v; }
    public String getCommentaireFinance() { return commentaireFinance; }
    public void setCommentaireFinance(String v) { commentaireFinance = v; }
    public Integer getIdDg() { return idDg; }
    public void setIdDg(Integer v) { idDg = v; }
    public LocalDateTime getDateDg() { return dateDg; }
    public void setDateDg(LocalDateTime v) { dateDg = v; }
    public String getCommentaireDg() { return commentaireDg; }
    public void setCommentaireDg(String v) { commentaireDg = v; }
    public Contrat getContratResultat() { return contratResultat; }
    public void setContratResultat(Contrat v) { contratResultat = v; }
}
