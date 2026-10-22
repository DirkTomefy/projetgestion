package s5.itu.projetgestion.model.recrutement;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "candidat")
public class Candidat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, length = 100)
    private String nom;
    @Column(length = 150)
    private String prenom;
    @Column(name = "date_naissance")
    private LocalDate dateNaissance;
    @Column(length = 255)
    private String adresse;
    @Column(length = 150)
    private String email;
    @Column(length = 30)
    private String numero;
    /** La même personne devient l'employé si elle est embauchée. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_personne", nullable = false)
    private Personne personne;
    public Integer getId() { return id; } public void setId(Integer id) { this.id = id; }
    public String getNom() { return nom; } public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; } public void setPrenom(String prenom) { this.prenom = prenom; }
    public LocalDate getDateNaissance() { return dateNaissance; } public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getAdresse() { return adresse; } public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public Personne getPersonne() { return personne; } public void setPersonne(Personne personne) { this.personne = personne; }
    public String getNumero() { return numero; } public void setNumero(String numero) { this.numero = numero; }
}
