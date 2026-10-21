package s5.itu.projetgestion.dto.auth;

import java.io.Serializable;
import java.util.List;

import s5.itu.projetgestion.model.auth.User;

public class SessionUser implements Serializable {

    private Integer id;
    private String login;
    private Integer idEmploye;
    private List<String> roles;

    public SessionUser() { }

    public static SessionUser from(User u) {
        SessionUser s = new SessionUser();
        s.id = u.getId();
        s.login = u.getLogin();
        s.idEmploye = u.getIdEmploye();
        s.roles = u.getRoles().stream().map(r -> r.getLibelle()).toList();
        return s;
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public Integer getIdEmploye() { return idEmploye; }
    public void setIdEmploye(Integer idEmploye) { this.idEmploye = idEmploye; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}