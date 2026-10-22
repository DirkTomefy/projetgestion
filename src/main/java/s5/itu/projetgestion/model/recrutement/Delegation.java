package s5.itu.projetgestion.model.recrutement;

import jakarta.persistence.*;
import s5.itu.projetgestion.model.auth.User;

import java.time.LocalDate;

@Entity
@Table(name = "delegation")
public class Delegation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user_delegant", nullable = false)
    private User userDelegant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user_delegue", nullable = false)
    private User userDelegue;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUserDelegant() {
        return userDelegant;
    }

    public void setUserDelegant(User userDelegant) {
        this.userDelegant = userDelegant;
    }

    public User getUserDelegue() {
        return userDelegue;
    }

    public void setUserDelegue(User userDelegue) {
        this.userDelegue = userDelegue;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }
}