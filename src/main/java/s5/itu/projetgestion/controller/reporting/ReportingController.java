package s5.itu.projetgestion.controller.reporting;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.Acces;
import s5.itu.projetgestion.dto.reporting.AccueilDto;
import s5.itu.projetgestion.dto.reporting.TableauDeBordDto;
import s5.itu.projetgestion.service.reporting.AccueilService;
import s5.itu.projetgestion.service.reporting.ExportService;
import s5.itu.projetgestion.service.reporting.ReportingService;

/** Accueil personnel, tableau de bord RH et exports CSV. */
@RestController
public class ReportingController {

    private final ReportingService reportingService;
    private final AccueilService accueilService;
    private final ExportService exportService;

    public ReportingController(ReportingService reportingService, AccueilService accueilService, ExportService exportService) {
        this.reportingService = reportingService;
        this.accueilService = accueilService;
        this.exportService = exportService;
    }

    @GetMapping("/api/accueil")
    public AccueilDto accueil(HttpSession session) {
        return accueilService.accueil(Acces.connecte(session));
    }

    @GetMapping("/api/reporting/tableau-de-bord")
    public TableauDeBordDto tableauDeBord(HttpSession session) {
        Acces.exiger(session, Acces.RH, Acces.DG, Acces.FINANCE, Acces.ADMIN);
        return reportingService.tableauDeBord();
    }

    /** Export CSV (séparateur « ; », UTF-8 avec BOM pour Excel) : personnel, besoins, candidatures, contrats. */
    @GetMapping("/api/reporting/export/{quoi}")
    public ResponseEntity<byte[]> export(@PathVariable String quoi, HttpSession session) {
        Acces.exiger(session, Acces.RH, Acces.DG, Acces.ADMIN);
        List<List<String>> lignes = exportService.lignes(quoi);
        StringBuilder sb = new StringBuilder("﻿");
        for (List<String> l : lignes) {
            sb.append(String.join(";", l.stream().map(ReportingController::cellule).toList())).append("\r\n");
        }
        String nom = quoi + "-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nom + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String cellule(String v) {
        if (v == null) return "";
        return v.contains(";") || v.contains("\"") || v.contains("\n") ? "\"" + v.replace("\"", "\"\"") + "\"" : v;
    }
}
