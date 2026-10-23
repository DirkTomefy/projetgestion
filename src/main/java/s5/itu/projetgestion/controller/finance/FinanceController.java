package s5.itu.projetgestion.controller.finance;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.config.Acces;
import s5.itu.projetgestion.dto.finance.BudgetDto;
import s5.itu.projetgestion.dto.finance.BudgetRequest;
import s5.itu.projetgestion.dto.finance.GrilleDto;
import s5.itu.projetgestion.dto.finance.GrilleRequest;
import s5.itu.projetgestion.service.finance.BudgetService;
import s5.itu.projetgestion.service.finance.GrilleSalarialeService;

/** Grille salariale et budgets des unités. Lecture : RH, Finance, DG, ADMIN, Manager ; saisie : Finance, ADMIN, DG ; validation : DG. */
@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final GrilleSalarialeService grilleService;
    private final BudgetService budgetService;

    public FinanceController(GrilleSalarialeService grilleService, BudgetService budgetService) {
        this.grilleService = grilleService;
        this.budgetService = budgetService;
    }

    @GetMapping("/grilles")
    public List<GrilleDto> grilles(HttpSession session) {
        lecture(session);
        return grilleService.lister();
    }

    @GetMapping("/grilles/en-vigueur")
    public ResponseEntity<GrilleDto> enVigueur(HttpSession session) {
        lecture(session);
        return grilleService.enVigueur().map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/grilles/{id}")
    public GrilleDto grille(@PathVariable Integer id, HttpSession session) {
        lecture(session);
        return grilleService.fiche(id);
    }

    /** Finance (ou ADMIN) : proposition à valider par le DG ; DG : grille validée tout de suite. */
    @PostMapping("/grilles")
    public ResponseEntity<GrilleDto> nouvelleGrille(@RequestBody GrilleRequest req, HttpSession session) {
        SessionUser su = ecriture(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(grilleService.creer(req, su));
    }

    @PostMapping("/grilles/{id}/valider")
    public GrilleDto validerGrille(@PathVariable Integer id, HttpSession session) {
        return grilleService.valider(id, Acces.exiger(session, Acces.DG));
    }

    @PostMapping("/grilles/{id}/refuser")
    public GrilleDto refuserGrille(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> body, HttpSession session) {
        return grilleService.refuser(id, body == null ? null : body.get("commentaire"), Acces.exiger(session, Acces.DG));
    }

    @GetMapping("/budgets")
    public List<BudgetDto> budgets(@RequestParam(required = false) Integer exercice, HttpSession session) {
        lecture(session);
        return budgetService.lister(exercice);
    }

    @PostMapping("/budgets")
    public ResponseEntity<BudgetDto> creerBudget(@RequestBody BudgetRequest req, HttpSession session) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.enregistrer(null, req, ecriture(session)));
    }

    @PutMapping("/budgets/{id}")
    public BudgetDto modifierBudget(@PathVariable Integer id, @RequestBody BudgetRequest req, HttpSession session) {
        return budgetService.enregistrer(id, req, ecriture(session));
    }

    @PostMapping("/budgets/{id}/valider")
    public BudgetDto validerBudget(@PathVariable Integer id, HttpSession session) {
        return budgetService.valider(id, Acces.exiger(session, Acces.DG));
    }

    @PostMapping("/budgets/{id}/refuser")
    public BudgetDto refuserBudget(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> body, HttpSession session) {
        return budgetService.refuser(id, body == null ? null : body.get("commentaire"), Acces.exiger(session, Acces.DG));
    }

    @DeleteMapping("/budgets/{id}")
    public ResponseEntity<Void> supprimerBudget(@PathVariable Integer id, HttpSession session) {
        budgetService.supprimer(id, ecriture(session));
        return ResponseEntity.noContent().build();
    }

    /** Saisie : Finance et ADMIN proposent, le DG décide (ses saisies sont validées d'office). */
    private static SessionUser ecriture(HttpSession session) {
        return Acces.exiger(session, Acces.FINANCE, Acces.DG, Acces.ADMIN);
    }

    private static void lecture(HttpSession session) {
        Acces.exiger(session, Acces.RH, Acces.FINANCE, Acces.DG, Acces.ADMIN, Acces.MANAGER);
    }
}
