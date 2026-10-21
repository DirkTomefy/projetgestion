package s5.itu.projetgestion.model.embauche;

import jakarta.persistence.*;

@Entity
@Table(name = "relance_condition")
public class RelanceCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proposition_condition", nullable = false)
    private PropositionCondition propositionCondition;

    @Column(name = "nouvelle_valeur", columnDefinition = "TEXT")
    private String nouvelleValeur;

    @Column(length = 30)
    private String statut;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public PropositionCondition getPropositionCondition() { return propositionCondition; }
    public void setPropositionCondition(PropositionCondition propositionCondition) { this.propositionCondition = propositionCondition; }
    public String getNouvelleValeur() { return nouvelleValeur; }
    public void setNouvelleValeur(String nouvelleValeur) { this.nouvelleValeur = nouvelleValeur; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
