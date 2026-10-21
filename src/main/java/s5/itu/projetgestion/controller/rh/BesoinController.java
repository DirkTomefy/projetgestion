package s5.itu.projetgestion.controller.rh;

import java.util.List;

import s5.itu.projetgestion.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.OptionDto;
import s5.itu.projetgestion.dto.SessionUser;
import s5.itu.projetgestion.dto.rh.BesoinDto;
import s5.itu.projetgestion.dto.rh.BesoinRequest;
import s5.itu.projetgestion.dto.rh.MotifRequest;
import s5.itu.projetgestion.repository.UserRepository;
import s5.itu.projetgestion.service.rh.BesoinService;

@RestController
@RequestMapping("/api/rh")
public class BesoinController {

    private final BesoinService besoinService;
    private final UserRepository userRepo;   // celui de P1 : adapte l'import

    public BesoinController(BesoinService besoinService, UserRepository userRepo) {
        this.besoinService = besoinService;
        this.userRepo = userRepo;
    }

    @GetMapping("/motifs")
    public List<OptionDto> motifs() { return besoinService.listerMotifs(); }

    @PostMapping("/motifs")
    public ResponseEntity<OptionDto> creerMotif(@RequestBody MotifRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(besoinService.creerMotif(req.libelle()));
    }

    // Recharger quand l'unité change : /api/rh/postes/actifs?idUnite=6
    @GetMapping("/postes/actifs")
    public List<OptionDto> postesActifs(@RequestParam Integer idUnite) {
        return besoinService.listerPostesActifs(idUnite);
    }

  
    @PostMapping("/besoins")
    public ResponseEntity<BesoinDto> creer(@RequestBody BesoinRequest req, HttpSession session) {
        User user = userConnecte(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(besoinService.creerBesoin(req, user));
    }

    // >>> LE SEUL ENDROIT À ADAPTER À LA CONNEXION DE P1 <<<
    private User userConnecte(HttpSession session) {
    SessionUser su = (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
    if (su == null)
        throw new IllegalArgumentException("Aucun utilisateur connecté.");
    return userRepo.findById(su.getId())
        .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
}
}