package s5.itu.projetgestion.model.recrutement;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "histo_proposition_embauche")
public class HistoPropositionEmbauche {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proposition_embauche", nullable = false)
    private PropositionEmbauche propositionEmbauche;

    @Column(name = "id_statut", nullable = false, length = 20)
    private String statut;

    @Column(nullable = false)
    private LocalDateTime date;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public PropositionEmbauche getPropositionEmbauche() { return propositionEmbauche; }
    public void setPropositionEmbauche(PropositionEmbauche propositionEmbauche) { this.propositionEmbauche = propositionEmbauche; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    @PrePersist
    void initializeDate() {
        if (date == null) {
            date = LocalDateTime.now();
        }
    }
}
