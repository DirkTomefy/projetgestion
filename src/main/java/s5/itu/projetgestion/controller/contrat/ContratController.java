package s5.itu.projetgestion.controller.contrat;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.dto.commun.OptionDto;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.commun.OptionDto;
import s5.itu.projetgestion.dto.contrat.ContratDto;
import s5.itu.projetgestion.dto.contrat.RenouvellementRequest;
import s5.itu.projetgestion.service.service.ContratService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/rh/contrats")
public class ContratController {

    private final ContratService service;

    public ContratController(ContratService service) {
        this.service = service;
    }

    @GetMapping("/employe/{idEmploye}")
    public List<ContratDto> parEmploye(@PathVariable Integer idEmploye) {
        return service.listerParEmploye(idEmploye);
    }

    @GetMapping("/{id:\\d+}")
    public ContratDto fiche(@PathVariable Integer id) {
        return service.fiche(id);
    }

    @PostMapping("/{id}/valider")
    public ContratDto valider(@PathVariable Integer id, HttpSession session) {
        exigerRHouDG(session);
        return service.valider(id);
    }

    @PostMapping("/{id}/signer")
    public ContratDto signer(@PathVariable Integer id, HttpSession session) {
        exigerRHouDG(session);
        return service.signer(id);
    }

    @PostMapping("/{id}/annuler")
    public ContratDto annuler(@PathVariable Integer id, HttpSession session) {
        exigerRHouDG(session);
        return service.annuler(id);
    }

    @PostMapping(value = "/{id}/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ContratDto deposer(@PathVariable Integer id,
            @RequestParam("fichier") MultipartFile fichier,
            HttpSession session) {
        exigerRHouDG(session);
        return service.deposerSigne(id, fichier);
    }

    private void exigerRHouDG(HttpSession session) {
        SessionUser su = (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
        if (su == null || !su.hasRole("RH") || !su.hasRole("DG"))
            throw new AccesRefuseException("Action réservée au rôle RH.");
    }

    @PostMapping("/{id}/renouveler")
    public ContratDto renouveler(@PathVariable Integer id,
            @RequestBody RenouvellementRequest req, HttpSession session) {
        exigerRHouDG(session);
        return service.renouveler(id, req);
    }

    @GetMapping("/types")
    public List<OptionDto> types() {
        return service.types();
    }

    @GetMapping("/echeances")
    public List<ContratDto> echeances(@RequestParam(defaultValue = "30") int jours) {
        return service.echeances(jours);
    }

    // ContratController

}