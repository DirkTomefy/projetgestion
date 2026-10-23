package s5.itu.projetgestion.controller.recrutement;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.model.auth.User;

import s5.itu.projetgestion.model.recrutement.Delegation;
import s5.itu.projetgestion.service.auth.UserService;
import s5.itu.projetgestion.service.recrutement.DelegationService;

@RestController
@RequestMapping({"/api/delegations", "/api/delegation"})
public class DelegationController {

    private DelegationService delegationService;
    private UserService userService;

    public DelegationController(DelegationService delegationService,
            UserService userService) {
        this.delegationService = delegationService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<String> deleguer(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                        @RequestParam Integer delegue, HttpSession session ){
        SessionUser sessionUser = (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
        if (sessionUser == null || sessionUser.getId() == null) {
            return ResponseEntity.status(401).body("Utilisateur non connecté");
        }
        User delegant = userService.getById(sessionUser.getId());
        User utilisateurDelegue = userService.getById(delegue);
        if (delegant == null || utilisateurDelegue == null) {
            return ResponseEntity.badRequest().body("Utilisateur introuvable");
        }
        Delegation delegation = new Delegation();
        delegation.setDateDebut(dateDebut);
        delegation.setDateFin(dateFin);
        delegation.setUserDelegant(delegant);
        delegation.setUserDelegue(utilisateurDelegue);
        try{
            this.delegationService.creerDelegation(delegation);
            return ResponseEntity.ok("Délégation enregistrée");
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }


    /** Mes délégations (données ou reçues) ; ?toutes=true pour la RH et l'ADMIN. */
    @org.springframework.web.bind.annotation.GetMapping
    public java.util.List<s5.itu.projetgestion.dto.recrutement.DelegationDto> lister(
            @RequestParam(defaultValue = "false") boolean toutes, HttpSession session) {
        SessionUser su = s5.itu.projetgestion.config.Acces.connecte(session);
        boolean tout = toutes && (su.hasRole("RH") || su.hasRole("ADMIN"));
        return delegationService.lister(tout ? null : su.getId());
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/annuler")
    public ResponseEntity<Void> annuler(@org.springframework.web.bind.annotation.PathVariable Integer id, HttpSession session) {
        delegationService.annuler(id, s5.itu.projetgestion.config.Acces.connecte(session));
        return ResponseEntity.noContent().build();
    }
}
