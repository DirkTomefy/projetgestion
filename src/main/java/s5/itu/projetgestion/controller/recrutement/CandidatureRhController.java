package s5.itu.projetgestion.controller.recrutement;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.recrutement.CandidatureFicheDto;
import s5.itu.projetgestion.dto.recrutement.CandidatureResumeDto;
import s5.itu.projetgestion.dto.recrutement.OffreCandidaturesDto;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.service.recrutement.CandidatureRhService;
import s5.itu.projetgestion.service.recrutement.OffreEmploiService;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/rh")
public class CandidatureRhController {

    private final CandidatureRhService service;

    public CandidatureRhController(CandidatureRhService service) {
        this.service = service;
    }

    @GetMapping("/offres-candidatures")
    public List<OffreCandidaturesDto> offres(HttpSession session) {
        verifierRh(session);
        return service.listerOffres();
    }

    @GetMapping("/offres/{idOffre}/candidatures")
    public List<CandidatureResumeDto> candidatures(@PathVariable Integer idOffre,
                                                   @RequestParam(required = false) String etat,
                                                   HttpSession session) {
        verifierRh(session);
        return service.listerCandidatures(idOffre, etat);
    }

    @GetMapping("/candidatures/{id}")
    public CandidatureFicheDto fiche(@PathVariable Integer id, HttpSession session) {
        verifierRh(session);
        return service.fiche(id);
    }

    @GetMapping("/candidatures/{id}/pieces/{idDocument}")
    public ResponseEntity<Resource> piece(@PathVariable Integer id, @PathVariable Integer idDocument,
                                          HttpSession session) {
        verifierRh(session);
        CandidatureRhService.PieceTelechargee piece = service.piece(id, idDocument);
        MediaType type = MediaTypeFactory.getMediaType(piece.nomFichier()).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(piece.nomFichier(), StandardCharsets.UTF_8).build().toString())
                .body(piece.fichier());
    }

    private static void verifierRh(HttpSession session) {
        SessionUser u = (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
        if (u == null || !u.hasRole(OffreEmploiService.ROLE_RH)) {
            throw new AccesRefuseException("Accès réservé au rôle RH");
        }
    }
}
