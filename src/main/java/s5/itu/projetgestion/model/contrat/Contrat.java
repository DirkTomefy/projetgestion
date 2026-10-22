package s5.itu.projetgestion.model.contrat;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.recrutement.PointCondition;
import s5.itu.projetgestion.model.recrutement.PropositionEmbauche;
import s5.itu.projetgestion.model.organisation.Affectation;
import s5.itu.projetgestion.model.commun.Document;
import s5.itu.projetgestion.model.contrat.TypeContrat;

import java.time.LocalDate;

@Entity
@Table(name = "contrat")
public class Contrat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_affectation", nullable = false)
    private Affectation affectation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proposition")
    private PropositionEmbauche proposition;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type", nullable = false)
    private TypeContrat type;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin_prevue")
    private LocalDate dateFinPrevue;

    @Column(name = "date_fin_effective")
    private LocalDate dateFinEffective;

    @Column(name = "statut_actuel", nullable = false, length = 30)
    private String statutActuel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrat_precedent")
    private Contrat contratPrecedent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_document")
    private Document document;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public Affectation getAffectation() { return affectation; }
    public void setAffectation(Affectation affectation) { this.affectation = affectation; }
    public PropositionEmbauche getProposition() { return proposition; }
    public void setProposition(PropositionEmbauche proposition) { this.proposition = proposition; }
    public TypeContrat getType() { return type; }
    public void setType(TypeContrat type) { this.type = type; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }
    public LocalDate getDateFinEffective() { return dateFinEffective; }
    public void setDateFinEffective(LocalDate dateFinEffective) { this.dateFinEffective = dateFinEffective; }
    public String getStatutActuel() { return statutActuel; }
    public void setStatutActuel(String statutActuel) { this.statutActuel = statutActuel; }
    public Contrat getContratPrecedent() { return contratPrecedent; }
    public void setContratPrecedent(Contrat contratPrecedent) { this.contratPrecedent = contratPrecedent; }
    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }
}
