package s5.itu.projetgestion.controller.organisation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.Acces;
import s5.itu.projetgestion.dto.organisation.EtapeValidationDto;
import s5.itu.projetgestion.dto.organisation.EtapeValidationRequest;
import s5.itu.projetgestion.dto.organisation.PosteDetailDto;
import s5.itu.projetgestion.dto.organisation.PosteRequest;
import s5.itu.projetgestion.dto.organisation.UniteDto;
import s5.itu.projetgestion.dto.organisation.UniteRequest;
import s5.itu.projetgestion.service.organisation.OrganisationService;

/** Bloc 0 : unités, organigramme, postes et circuit de validation. Lecture : tout connecté ; écriture : ADMIN, RH. */
@RestController
@RequestMapping("/api/organisation")
public class OrganisationController {

    private final OrganisationService service;

    public OrganisationController(OrganisationService service) {
        this.service = service;
    }

    @GetMapping("/unites")
    public List<UniteDto> unites(HttpSession session) {
        Acces.connecte(session);
        return service.unites();
    }

    @GetMapping("/organigramme")
    public List<UniteDto> organigramme(HttpSession session) {
        Acces.connecte(session);
        return service.organigramme();
    }

    @PostMapping("/unites")
    public ResponseEntity<UniteDto> creerUnite(@RequestBody UniteRequest req, HttpSession session) {
        ecriture(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.enregistrerUnite(null, req));
    }

    @PutMapping("/unites/{id}")
    public UniteDto modifierUnite(@PathVariable Integer id, @RequestBody UniteRequest req, HttpSession session) {
        ecriture(session);
        return service.enregistrerUnite(id, req);
    }

    @DeleteMapping("/unites/{id}")
    public ResponseEntity<Void> supprimerUnite(@PathVariable Integer id, HttpSession session) {
        ecriture(session);
        service.supprimerUnite(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/postes")
    public List<PosteDetailDto> postes(@RequestParam(required = false) String etat, @RequestParam(required = false) String occupation,
                                       @RequestParam(required = false) Integer idUnite, @RequestParam(required = false) String q,
                                       HttpSession session) {
        Acces.connecte(session);
        return service.postes(etat, occupation, idUnite, q);
    }

    @GetMapping("/postes/{id}")
    public PosteDetailDto poste(@PathVariable Integer id, HttpSession session) {
        Acces.connecte(session);
        return service.poste(id);
    }

    @PostMapping("/postes")
    public ResponseEntity<PosteDetailDto> creerPoste(@RequestBody PosteRequest req, HttpSession session) {
        ecriture(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creerPoste(req));
    }

    @PutMapping("/postes/{id}")
    public PosteDetailDto modifierPoste(@PathVariable Integer id, @RequestBody PosteRequest req, HttpSession session) {
        ecriture(session);
        return service.modifierPoste(id, req);
    }

    @DeleteMapping("/postes/{id}")
    public PosteDetailDto supprimerPoste(@PathVariable Integer id, HttpSession session) {
        ecriture(session);
        return service.supprimerPoste(id);
    }

    @GetMapping("/circuit-validation")
    public List<EtapeValidationDto> circuit(HttpSession session) {
        Acces.connecte(session);
        return service.circuit();
    }

    @PostMapping("/circuit-validation")
    public ResponseEntity<EtapeValidationDto> ajouterEtape(@RequestBody EtapeValidationRequest req, HttpSession session) {
        ecriture(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.enregistrerEtape(null, req));
    }

    @PutMapping("/circuit-validation/{id}")
    public EtapeValidationDto modifierEtape(@PathVariable Integer id, @RequestBody EtapeValidationRequest req, HttpSession session) {
        ecriture(session);
        return service.enregistrerEtape(id, req);
    }

    @DeleteMapping("/circuit-validation/{id}")
    public ResponseEntity<Void> supprimerEtape(@PathVariable Integer id, HttpSession session) {
        ecriture(session);
        service.supprimerEtape(id);
        return ResponseEntity.noContent().build();
    }

    private static void ecriture(HttpSession session) {
        Acces.exiger(session, Acces.ADMIN, Acces.RH);
    }
}
