package s5.itu.projetgestion.controller.organisation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.Acces;
import s5.itu.projetgestion.dto.organisation.CoordonneesBancairesDto;
import s5.itu.projetgestion.dto.organisation.EmployeDossierDto;
import s5.itu.projetgestion.dto.organisation.EmployeRequest;
import s5.itu.projetgestion.dto.organisation.EmployeResumeDto;
import s5.itu.projetgestion.dto.organisation.MouvementRequest;
import s5.itu.projetgestion.service.organisation.EmployeGestionService;

/** Gestion du personnel (RH) : dossier, saisie, mutation, départ, coordonnées bancaires. */
@RestController
@RequestMapping("/api/rh/employes")
public class EmployeGestionController {

    private final EmployeGestionService service;

    public EmployeGestionController(EmployeGestionService service) {
        this.service = service;
    }

    /** Tout le personnel, y compris les employés partis. */
    @GetMapping("/tous")
    public List<EmployeResumeDto> tous(@RequestParam(required = false) String q, HttpSession session) {
        Acces.exiger(session, Acces.RH, Acces.DG, Acces.ADMIN);
        return service.listerTous(q);
    }

    @GetMapping("/{id:\\d+}/dossier")
    public EmployeDossierDto dossier(@PathVariable Integer id, HttpSession session) {
        Acces.exiger(session, Acces.RH, Acces.DG, Acces.ADMIN);
        return service.dossier(id);
    }

    @PostMapping
    public ResponseEntity<EmployeDossierDto> creer(@RequestBody EmployeRequest req, HttpSession session) {
        Acces.exiger(session, Acces.RH);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(req));
    }

    @PutMapping("/{id:\\d+}")
    public EmployeDossierDto modifier(@PathVariable Integer id, @RequestBody EmployeRequest req, HttpSession session) {
        Acces.exiger(session, Acces.RH);
        return service.modifier(id, req);
    }

    @PostMapping("/{id:\\d+}/mutation")
    public EmployeDossierDto mutation(@PathVariable Integer id, @RequestBody MouvementRequest req, HttpSession session) {
        Acces.exiger(session, Acces.RH);
        return service.muter(id, req);
    }

    @PostMapping("/{id:\\d+}/depart")
    public EmployeDossierDto depart(@PathVariable Integer id, @RequestBody MouvementRequest req, HttpSession session) {
        Acces.exiger(session, Acces.RH);
        return service.depart(id, req);
    }

    @GetMapping("/{id:\\d+}/banque")
    public List<CoordonneesBancairesDto> banque(@PathVariable Integer id, HttpSession session) {
        Acces.exiger(session, Acces.RH, Acces.ADMIN);
        return service.banque(id);
    }

    @PostMapping("/{id:\\d+}/banque")
    public List<CoordonneesBancairesDto> enregistrerBanque(@PathVariable Integer id, @RequestBody CoordonneesBancairesDto req, HttpSession session) {
        return service.enregistrerBanque(id, req, Acces.exiger(session, Acces.RH).getId());
    }
}
