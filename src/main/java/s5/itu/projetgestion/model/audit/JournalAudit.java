package s5.itu.projetgestion.model.audit;

import java.time.LocalDateTime;

import jakarta.persistence.*;

/** Une action enregistrée dans le journal d'audit (toute modification passée par l'API). */
@Entity
@Table(name = "journal_audit")
public class JournalAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "date_action", nullable = false)
    private LocalDateTime dateAction = LocalDateTime.now();
    @Column(name = "id_user")
    private Integer idUser;
    @Column(length = 100)
    private String login;
    @Column(nullable = false, length = 200)
    private String action;
    @Column(nullable = false, length = 10)
    private String methode;
    @Column(nullable = false, length = 300)
    private String chemin;
    @Column(name = "statut_http", nullable = false)
    private Integer statutHttp;
    @Column(name = "adresse_ip", length = 60)
    private String adresseIp;

    public Integer getId() { return id; }
    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }
    public Integer getIdUser() { return idUser; }
    public void setIdUser(Integer idUser) { this.idUser = idUser; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getMethode() { return methode; }
    public void setMethode(String methode) { this.methode = methode; }
    public String getChemin() { return chemin; }
    public void setChemin(String chemin) { this.chemin = chemin; }
    public Integer getStatutHttp() { return statutHttp; }
    public void setStatutHttp(Integer statutHttp) { this.statutHttp = statutHttp; }
    public String getAdresseIp() { return adresseIp; }
    public void setAdresseIp(String adresseIp) { this.adresseIp = adresseIp; }
}
