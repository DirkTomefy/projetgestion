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

    // la base met NOW() toute seule ; Hibernate relit la valeur juste après l'insertion
    @org.hibernate.annotations.Generated(event = org.hibernate.generator.EventType.INSERT)
    @Column(insertable = false, updatable = false)
    private LocalDateTime date;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Contrat getContrat() { return contrat; }
    public void setContrat(Contrat contrat) { this.contrat = contrat; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDateTime getDate() { return date; }
}
