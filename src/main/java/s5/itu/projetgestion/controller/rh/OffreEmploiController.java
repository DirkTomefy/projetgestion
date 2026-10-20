package s5.itu.projetgestion.controller.rh;

import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.SessionUser;
import s5.itu.projetgestion.dto.rh.BesoinOffreInfo;
import s5.itu.projetgestion.dto.rh.OffreEmploiForm;
import s5.itu.projetgestion.err.ProjetgestionException;
import s5.itu.projetgestion.model.rh.OffreEmploi;
import s5.itu.projetgestion.model.rh.TypeContrat;
import s5.itu.projetgestion.service.rh.BesoinOffreLookupService;
import s5.itu.projetgestion.service.rh.FileStorageService;
import s5.itu.projetgestion.service.rh.OffreEmploiService;
import s5.itu.projetgestion.service.rh.OffrePdfService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/rh/offres")
public class OffreEmploiController {

    private final OffreEmploiService offreService;
    private final BesoinOffreLookupService besoinLookup;
    private final OffrePdfService pdfService;
    private final FileStorageService fileStorageService;

    public OffreEmploiController(OffreEmploiService offreService,
                                 BesoinOffreLookupService besoinLookup,
                                 OffrePdfService pdfService,
                                 FileStorageService fileStorageService) {
        this.offreService = offreService;
        this.besoinLookup = besoinLookup;
        this.pdfService = pdfService;
        this.fileStorageService = fileStorageService;
    }


    @GetMapping
    public String liste(HttpSession session, Model model, RedirectAttributes ra) {
        if (!estRh(session)) return refuser(ra);
        List<OffreEmploi> offres = offreService.findAll();
        Map<Integer, BesoinOffreInfo> besoins = new HashMap<>();
        for (OffreEmploi o : offres) {
            besoinLookup.findById(o.getIdBesoin()).ifPresent(b -> besoins.put(b.id(), b));
        }
        model.addAttribute("offres", offres);
        model.addAttribute("besoins", besoins);
        return "offres/list";
    }

    @GetMapping("/besoins")
    public String besoinsOuverts(HttpSession session, Model model, RedirectAttributes ra) {
        if (!estRh(session)) return refuser(ra);
        model.addAttribute("besoins", besoinLookup.findRecrutementOuvert());
        return "offres/besoins";
    }


    @GetMapping("/nouvelle")
    public String formulaire(@RequestParam Integer idBesoin, HttpSession session,
                             Model model, RedirectAttributes ra) {
        if (!estRh(session)) return refuser(ra);
        try {
            OffreEmploiForm form = offreService.nouveauFormulaire(idBesoin);
            return afficherFormulaire(model, form, offreService.getBesoinOuvert(idBesoin), null);
        } catch (ProjetgestionException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/rh/offres/besoins";
        }
    }

    @PostMapping("/nouvelle")
    public String valider(@ModelAttribute("form") OffreEmploiForm form, HttpSession session,
                          Model model, RedirectAttributes ra) {
        if (!estRh(session)) return refuser(ra);
        try {
            OffreEmploi o = offreService.creer(form, currentUser(session));
            ra.addFlashAttribute("message", "Offre " + o.getRef() + " enregistrée (" + o.getEtat() + ")");
            return "redirect:/rh/offres";
        } catch (ProjetgestionException | IllegalArgumentException e) {
            BesoinOffreInfo besoin = besoinLookup.findById(form.getIdBesoin()).orElse(null);
            if (besoin == null) {
                ra.addFlashAttribute("error", e.getMessage());
                return "redirect:/rh/offres/besoins";
            }
            return afficherFormulaire(model, form, besoin, e.getMessage());
        }
    }

    @PostMapping(value = "/apercu", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String apercu(@ModelAttribute OffreEmploiForm form, HttpSession session) {
        if (!estRh(session)) return "Action réservée au rôle RH";
        BesoinOffreInfo b = offreService.getBesoinOuvert(form.getIdBesoin());
        TypeContrat tc = form.getIdTypeContrat() == null ? null :
                offreService.typesContrat().stream()
                        .filter(t -> t.getId().equals(form.getIdTypeContrat())).findFirst().orElse(null);
        return pdfService.renderHtml(offreService.variablesModele(b, form.getReference(),
                form.getDatePublication(), form.getDateLimiteCandidature(), tc,
                form.getDescription(), form.getProfil()));
    }


    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> pdf(@PathVariable Integer id) {
        OffreEmploi o = offreService.getById(id);
        if (o.getDocument() == null) return ResponseEntity.notFound().build();
        Resource r = fileStorageService.loadAsResource(o.getDocument().getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"offre_" + o.getRef() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(r);
    }

    @PostMapping("/{id}/publier")
    public String publier(@PathVariable Integer id, HttpSession session, RedirectAttributes ra) {
        if (!estRh(session)) return refuser(ra);
        try {
            OffreEmploi o = offreService.publier(id, currentUser(session));
            ra.addFlashAttribute("message", "Offre " + o.getRef() + " publiée");
        } catch (ProjetgestionException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rh/offres";
    }


    private String afficherFormulaire(Model model, OffreEmploiForm form, BesoinOffreInfo besoin, String error) {
        model.addAttribute("form", form);
        model.addAttribute("besoin", besoin);
        model.addAttribute("typesContrat", offreService.typesContrat());
        if (error != null) model.addAttribute("error", error);
        return "offres/form";
    }

    private static SessionUser currentUser(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
    }

    private static boolean estRh(HttpSession session) {
        SessionUser u = currentUser(session);
        return u != null && u.hasRole(OffreEmploiService.ROLE_RH);
    }

    private static String refuser(RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Page réservée au rôle RH");
        return "redirect:/";
    }
}
