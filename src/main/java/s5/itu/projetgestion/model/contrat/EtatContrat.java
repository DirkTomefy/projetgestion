package s5.itu.projetgestion.model.contrat;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "etat_contrat")
public class EtatContrat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_contrat", nullable = false)
    private Contrat contrat;

    @Column(nullable = false, length = 30)
    private String statut;

    @Column(nullable = false)
    private LocalDateTime date;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Contrat getContrat() { return contrat; }
    public void setContrat(Contrat contrat) { this.contrat = contrat; }
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
