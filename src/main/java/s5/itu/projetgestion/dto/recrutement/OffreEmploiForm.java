package s5.itu.projetgestion.dto.recrutement;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class OffreEmploiForm {

    private Integer idBesoin;
    /** Poste du besoin couvert par l'offre (obligatoire si le besoin demande plusieurs postes). */
    private Integer idPoste;

    private String reference;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate datePublication = LocalDate.now();

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateLimiteCandidature;

    private Integer idTypeContrat;

    private String etat = "PUBLIEE";

    private String description;
    private String profil;

    public Integer getIdBesoin() { return idBesoin; }
    public void setIdBesoin(Integer idBesoin) { this.idBesoin = idBesoin; }
    public Integer getIdPoste() { return idPoste; }
    public void setIdPoste(Integer idPoste) { this.idPoste = idPoste; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public LocalDate getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDate datePublication) { this.datePublication = datePublication; }
    public LocalDate getDateLimiteCandidature() { return dateLimiteCandidature; }
    public void setDateLimiteCandidature(LocalDate d) { this.dateLimiteCandidature = d; }
    public Integer getIdTypeContrat() { return idTypeContrat; }
    public void setIdTypeContrat(Integer idTypeContrat) { this.idTypeContrat = idTypeContrat; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getProfil() { return profil; }
    public void setProfil(String profil) { this.profil = profil; }
}
