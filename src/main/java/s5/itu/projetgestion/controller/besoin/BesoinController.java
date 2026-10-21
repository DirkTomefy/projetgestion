package s5.itu.projetgestion.controller.besoin;

import java.util.List;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import s5.itu.projetgestion.config.SessionKeys;
import s5.itu.projetgestion.dto.commun.OptionDto;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.besoin.BesoinResponse;
import s5.itu.projetgestion.dto.besoin.BesoinDto;
import s5.itu.projetgestion.dto.besoin.BesoinRequest;
import s5.itu.projetgestion.dto.besoin.MotifRequest;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.repository.auth.UserRepository;
import s5.itu.projetgestion.service.besoin.BesoinService;

@RestController 
@RequestMapping
public class BesoinController {
    
    private final BesoinService besoinService;
    private final UserRepository userRepository;

    public BesoinController(BesoinService besoinService, UserRepository userRepository){
        this.besoinService = besoinService;
        this.userRepository = userRepository;
    } 


    @GetMapping("/api/besoin")
    public List<BesoinResponse> list(){
        return this.besoinService.findAll();
    }


    @GetMapping("/api/besoin/{id}")
    public BesoinResponse show(@PathVariable Integer id){
        try{
            return this.besoinService.fiche(id);
        }catch(Exception e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    } 

    @GetMapping("/api/rh/motifs")
    public List<OptionDto> motifs() { return besoinService.listerMotifs(); }

    @PostMapping("/api/rh/motifs")
    public ResponseEntity<OptionDto> creerMotif(@RequestBody MotifRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(besoinService.creerMotif(request.libelle()));
    }

    @GetMapping("/api/rh/postes/actifs")
    public List<OptionDto> postesActifs(@RequestParam Integer idUnite) {
        return besoinService.listerPostesActifs(idUnite);
    }

    @PostMapping("/api/rh/besoins")
    public ResponseEntity<BesoinDto> creer(@RequestBody BesoinRequest request, HttpSession session) {
        SessionUser sessionUser = (SessionUser) session.getAttribute(SessionKeys.CURRENT_USER);
        if (sessionUser == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Aucun utilisateur connecté.");
        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable."));
        return ResponseEntity.status(HttpStatus.CREATED).body(besoinService.creerBesoin(request, user));
    }

}
