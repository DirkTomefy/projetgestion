package s5.itu.projetgestion.controller.admin;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.Acces;
import s5.itu.projetgestion.model.audit.JournalAudit;
import s5.itu.projetgestion.service.audit.AuditService;

/** Consultation du journal d'audit (ADMIN, DG). */
@RestController
public class AuditController {

    private final AuditService service;

    public AuditController(AuditService service) {
        this.service = service;
    }

    @GetMapping("/api/admin/audit")
    public List<JournalAudit> journal(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au,
                                      @RequestParam(required = false) String resultat,
                                      @RequestParam(defaultValue = "200") int limite,
                                      HttpSession session) {
        Acces.exiger(session, Acces.ADMIN, Acces.DG);
        return service.rechercher(q, du, au, resultat, limite);
    }
}
