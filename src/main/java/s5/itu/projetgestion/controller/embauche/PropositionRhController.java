package s5.itu.projetgestion.controller.embauche;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.embauche.*;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.err.PropositionException;
import s5.itu.projetgestion.service.embauche.PropositionEmbaucheService;
import s5.itu.projetgestion.service.finance.FourchetteSalariale;
import s5.itu.projetgestion.service.finance.GrilleSalarialeService;
import s5.itu.projetgestion.service.recrutement.OffreEmploiService;

import java.time.LocalDate;
import java.util.List;

/**
 * P4 : propositions d'embauche (RH).
 * Les candidatures à traiter viennent de GET /api/rh/candidatures-retenues (P5).
 */
@RestController
@RequestMapping("/api/rh/propositions")
public class PropositionRhController {

    private final PropositionEmbaucheService service;
    private final GrilleSalarialeService grilleService;

    public PropositionRhController(PropositionEmbaucheService service, GrilleSalarialeService grilleService) {
        this.service = service;
        this.grilleService = grilleService;
    }

    /** Liste, filtrable par statut (PROPOSER, ACCEPTER, REFUSER, EXPIREE). */
    @GetMapping
    public List<PropositionResumeDto> liste(@RequestParam(required = false) String statut, HttpSession session) {
        rh(session);
        return service.lister(statut);
    }

    /** Sièges possibles, fourchettes salariales et valeurs par défaut pour une candidature RETENUE. */
    @GetMapping("/formulaire")
    public FormulairePropositionDto formulaire(@RequestParam Integer idCandidature, HttpSession session) {
        rh(session);
        return service.formulaire(idCandidature);
    }

    /** Fourchette salariale d'un poste à une date (contrôle à la saisie). */
    @GetMapping("/fourchette")
    public FourchetteSalariale fourchette(@RequestParam Integer idPoste,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                          HttpSession session) {
        rh(session);
        return grilleService.fourchette(idPoste, date)
                .orElseThrow(() -> PropositionException.introuvable("Aucune grille salariale en vigueur pour ce poste"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropositionDto creer(@Valid @RequestBody PropositionRequest req, HttpSession session) {
        return service.creer(req, rh(session));
    }

    /** Fiche : conditions, contre-propositions, historique, lien de réponse du candidat. */
    @GetMapping("/{id}")
    public PropositionDto fiche(@PathVariable Integer id, HttpSession session) {
        rh(session);
        return service.fiche(id);
    }

    @PostMapping("/{id}/contre-proposition")
    public PropositionDto contreProposer(@PathVariable Integer id, @Valid @RequestBody ContrePropositionRequest req,
                                         HttpSession session) {
        return service.contreProposer(id, req, rh(session));
    }

    @PostMapping("/{id}/retrait")
    public PropositionDto retirer(@PathVariable Integer id, @Valid @RequestBody MotifRequest req, HttpSession session) {
        return service.retirer(id, req, rh(session));
    }

    private static SessionUser rh(HttpSession session) {
        SessionUser u = (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
        if (u == null || !u.hasRole(OffreEmploiService.ROLE_RH)) {
            throw new AccesRefuseException("Accès réservé au rôle RH");
        }
        return u;
    }
}
