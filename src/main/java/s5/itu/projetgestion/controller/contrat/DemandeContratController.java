package s5.itu.projetgestion.controller.contrat;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.Acces;
import s5.itu.projetgestion.dto.contrat.DecisionContratRequest;
import s5.itu.projetgestion.dto.contrat.DemandeContratDto;
import s5.itu.projetgestion.dto.contrat.DemandeContratRequest;
import s5.itu.projetgestion.service.service.DemandeContratService;

/** Nouveau contrat / avenant : demande (RH, manager, employé), instruction RH, avis Finance, décision du DG. */
@RestController
@RequestMapping("/api/contrats")
public class DemandeContratController {

    private final DemandeContratService service;

    public DemandeContratController(DemandeContratService service) { this.service = service; }

    /** vue = a-traiter | mes | toutes (RH, DG, Finance). */
    @GetMapping("/demandes")
    public List<DemandeContratDto> lister(@RequestParam(defaultValue = "mes") String vue,
                                          @RequestParam(required = false) Integer idEmploye, HttpSession session) {
        return service.lister(vue, idEmploye, Acces.connecte(session));
    }

    @GetMapping("/demandes/{id:\\d+}")
    public DemandeContratDto fiche(@PathVariable Integer id, HttpSession session) {
        return service.fiche(id, Acces.connecte(session));
    }

    /** Contrats pour lesquels l'utilisateur peut faire une demande. */
    @GetMapping("/demandes/contrats")
    public List<DemandeContratService.ContratEligible> eligibles(HttpSession session) {
        return service.eligibles(Acces.connecte(session));
    }

    @GetMapping("/mon-contrat")
    public ResponseEntity<DemandeContratService.MonContrat> monContrat(HttpSession session) {
        var m = service.monContrat(Acces.connecte(session));
        return m == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(m);
    }

    @PostMapping("/demandes")
    public ResponseEntity<DemandeContratDto> creer(@RequestBody DemandeContratRequest req, HttpSession session) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(req, Acces.connecte(session)));
    }

    @PostMapping("/demandes/{id:\\d+}/instruire")
    public DemandeContratDto instruire(@PathVariable Integer id, @RequestBody DecisionContratRequest req, HttpSession session) {
        return service.instruire(id, req, Acces.exiger(session, Acces.RH));
    }

    @PostMapping("/demandes/{id:\\d+}/finance")
    public DemandeContratDto finance(@PathVariable Integer id, @RequestBody DecisionContratRequest req, HttpSession session) {
        return service.finance(id, req, Acces.exiger(session, Acces.FINANCE));
    }

    @PostMapping("/demandes/{id:\\d+}/decider")
    public DemandeContratDto decider(@PathVariable Integer id, @RequestBody DecisionContratRequest req, HttpSession session) {
        return service.decider(id, req, Acces.exiger(session, Acces.DG));
    }

    @PostMapping("/demandes/{id:\\d+}/annuler")
    public DemandeContratDto annuler(@PathVariable Integer id, HttpSession session) {
        return service.annuler(id, Acces.connecte(session));
    }
}
