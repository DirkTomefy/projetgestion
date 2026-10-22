package s5.itu.projetgestion.controller.organisation;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.organisation.EmployeFicheDto;
import s5.itu.projetgestion.dto.organisation.EmployeResumeDto;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.service.organisation.EmployeFicheService;

/** Personnel : liste et fiche employé (réservé RH). */
@RestController
@RequestMapping("/api/rh/employes")
public class EmployeController {

    private final EmployeFicheService service;

    public EmployeController(EmployeFicheService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmployeResumeDto> lister(@RequestParam(required = false) String q, HttpSession session) {
        exigerRh(session);
        return service.lister(q);
    }

    @GetMapping("/{id:\\d+}")
    public EmployeFicheDto fiche(@PathVariable Integer id, HttpSession session) {
        exigerRh(session);
        return service.fiche(id);
    }

    private static void exigerRh(HttpSession session) {
        SessionUser su = (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
        if (su == null || !su.hasRole("RH")) {
            throw new AccesRefuseException("Consultation du personnel réservée au rôle RH");
        }
    }
}
