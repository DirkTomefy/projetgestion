package s5.itu.projetgestion.controller.organisation;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import s5.itu.projetgestion.config.Acces;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.organisation.FichePosteDto;
import s5.itu.projetgestion.dto.organisation.FichePosteRequest;
import s5.itu.projetgestion.repository.organisation.FichePosteRepository;
import s5.itu.projetgestion.service.organisation.FichePosteService;

import java.util.List;

/** Fiches de poste : gestion par la RH, consultation par chacun pour son poste. */
@RestController
@RequestMapping("/api/fiches-poste")
public class FichePosteController {

    public record LigneDto(Integer idMetier, String metier, String categorie, Integer idFiche, Integer version,
                           java.time.LocalDateTime dateMaj, long nbPostes, long nbPostesPropres) { }

    private final FichePosteService service;

    public FichePosteController(FichePosteService service) { this.service = service; }

    /** Catalogue : tous les métiers et l'état de leur fiche modèle (RH, DG, ADMIN). */
    @GetMapping
    public List<LigneDto> catalogue(HttpSession session) {
        Acces.exiger(session, Acces.RH, Acces.DG, Acces.ADMIN);
        return service.catalogue().stream().map(FichePosteController::ligne).toList();
    }

    private static LigneDto ligne(FichePosteRepository.LigneCatalogue l) {
        return new LigneDto(l.getIdMetier(), l.getMetier(), l.getCategorie(), l.getIdFiche(), l.getVersion(),
                l.getDateMaj(), l.getNbPostes() == null ? 0 : l.getNbPostes(), l.getNbPostesPropres() == null ? 0 : l.getNbPostesPropres());
    }

    @GetMapping("/modele-standard")
    public FichePosteRequest modeleStandard(HttpSession session) {
        Acces.exiger(session, Acces.RH);
        return service.modeleStandard();
    }

    @GetMapping("/metier/{id}")
    public FichePosteDto duMetier(@PathVariable Integer id, HttpSession session) {
        Acces.exiger(session, Acces.RH, Acces.DG, Acces.ADMIN);
        return service.duMetier(id);
    }

    @PutMapping("/metier/{id}")
    public FichePosteDto enregistrerMetier(@PathVariable Integer id, @RequestBody FichePosteRequest req, HttpSession session) {
        SessionUser su = Acces.exiger(session, Acces.RH);
        return service.enregistrerMetier(id, req, su);
    }

    /** Fiche applicable à un poste (propre au poste, sinon celle du métier). 204 si aucune. */
    @GetMapping("/poste/{id}")
    public ResponseEntity<FichePosteDto> dePoste(@PathVariable Integer id, HttpSession session) {
        FichePosteDto f = service.dePoste(id, Acces.connecte(session));
        return f == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(f);
    }

    @PutMapping("/poste/{id}")
    public FichePosteDto enregistrerPoste(@PathVariable Integer id, @RequestBody FichePosteRequest req, HttpSession session) {
        SessionUser su = Acces.exiger(session, Acces.RH);
        return service.enregistrerPoste(id, req, su);
    }

    @DeleteMapping("/poste/{id}")
    public ResponseEntity<Void> supprimerPoste(@PathVariable Integer id, HttpSession session) {
        Acces.exiger(session, Acces.RH);
        service.supprimerPoste(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/poste/{id}/pdf")
    public ResponseEntity<byte[]> pdfPoste(@PathVariable Integer id, HttpSession session) {
        return pdf(service.pdfDePoste(id, Acces.connecte(session)), "fiche-poste-" + id);
    }

    /** La fiche du poste de l'employé connecté. 204 s'il n'occupe pas de poste ou si aucune fiche n'existe. */
    @GetMapping("/ma-fiche")
    public ResponseEntity<FichePosteDto> maFiche(HttpSession session) {
        FichePosteDto f = service.maFiche(Acces.connecte(session));
        return f == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(f);
    }

    @GetMapping("/ma-fiche/pdf")
    public ResponseEntity<byte[]> pdfMaFiche(HttpSession session) {
        return pdf(service.pdfMaFiche(Acces.connecte(session)), "ma-fiche-de-poste");
    }

    private static ResponseEntity<byte[]> pdf(byte[] contenu, String nom) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nom + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenu);
    }
}
