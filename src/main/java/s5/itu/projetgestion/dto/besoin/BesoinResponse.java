package s5.itu.projetgestion.dto.besoin;

import java.time.LocalDate;

import s5.itu.projetgestion.model.besoin.Besoin;

public class BesoinResponse {
    private Integer id;
    private String reference;
    private String typeBesoin;
    private Integer motifId;
    private String motifLibelle;
    private Integer uniteOrgaId;
    private String uniteOrgaCode;
    private String uniteOrgaLibelle;
    private LocalDate dateSouhaitee;
    private Integer userId;
    private String userLogin;
    private Integer effectif;
    private String etatActuel;
    private Double budgetEstime;
    private String precisionMotif;

    public BesoinResponse fromBesoin(Besoin besoin) {
        BesoinResponse response = new BesoinResponse();
        response.setId(besoin.getId());
        response.setReference(besoin.getReference());
        response.setTypeBesoin(besoin.getTypeBesoin());
        response.setMotifId(besoin.getMotif().getId());
        response.setMotifLibelle(besoin.getMotif().getLibelle());
        response.setUniteOrgaId(besoin.getUniteOrga().getId());
        response.setUniteOrgaCode(besoin.getUniteOrga().getCode());
        response.setUniteOrgaLibelle(besoin.getUniteOrga().getLibelle());
        response.setDateSouhaitee(besoin.getDateSouhaitee());
        response.setUserId(besoin.getUser().getId());
        response.setUserLogin(besoin.getUser().getLogin());
        response.setEffectif(besoin.getEffectif());
        response.setEtatActuel(besoin.getEtatActuel());
        response.setBudgetEstime(besoin.getBudgetEstime());
        response.setPrecisionMotif(besoin.getPrecisionMotif());
        return response;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getTypeBesoin() {
        return typeBesoin;
    }

    public void setTypeBesoin(String typeBesoin) {
        this.typeBesoin = typeBesoin;
    }

    public Integer getMotifId() {
        return motifId;
    }

    public void setMotifId(Integer motifId) {
        this.motifId = motifId;
    }

    public String getMotifLibelle() {
        return motifLibelle;
    }

    public void setMotifLibelle(String motifLibelle) {
        this.motifLibelle = motifLibelle;
    }

    public Integer getUniteOrgaId() {
        return uniteOrgaId;
    }

    public void setUniteOrgaId(Integer uniteOrgaId) {
        this.uniteOrgaId = uniteOrgaId;
    }

    public String getUniteOrgaCode() {
        return uniteOrgaCode;
    }

    public void setUniteOrgaCode(String uniteOrgaCode) {
        this.uniteOrgaCode = uniteOrgaCode;
    }

    public String getUniteOrgaLibelle() {
        return uniteOrgaLibelle;
    }

    public void setUniteOrgaLibelle(String uniteOrgaLibelle) {
        this.uniteOrgaLibelle = uniteOrgaLibelle;
    }

    public LocalDate getDateSouhaitee() {
        return dateSouhaitee;
    }

    public void setDateSouhaitee(LocalDate dateSouhaitee) {
        this.dateSouhaitee = dateSouhaitee;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public Integer getEffectif() {
        return effectif;
    }

    public void setEffectif(Integer effectif) {
        this.effectif = effectif;
    }

    public String getEtatActuel() {
        return etatActuel;
    }

    public void setEtatActuel(String etatActuel) {
        this.etatActuel = etatActuel;
    }

    public Double getBudgetEstime() {
        return budgetEstime;
    }

    public void setBudgetEstime(Double budgetEstime) {
        this.budgetEstime = budgetEstime;
    }

    public String getPrecisionMotif() {
        return precisionMotif;
    }

    public void setPrecisionMotif(String precisionMotif) {
        this.precisionMotif = precisionMotif;
    }
}
