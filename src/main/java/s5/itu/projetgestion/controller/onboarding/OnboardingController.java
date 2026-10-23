package s5.itu.projetgestion.controller.onboarding;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import s5.itu.projetgestion.config.Acces;
import s5.itu.projetgestion.dto.onboarding.ModeleOnboardingDto;
import s5.itu.projetgestion.dto.onboarding.ModeleOnboardingRequest;
import s5.itu.projetgestion.dto.onboarding.OnboardingDto;
import s5.itu.projetgestion.dto.onboarding.TacheDto;
import s5.itu.projetgestion.dto.onboarding.TacheRequest;
import s5.itu.projetgestion.service.onboarding.OnboardingService;

/** Onboarding : suivi (RH), tâches confiées (tout utilisateur), modèles (RH, ADMIN). */
@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService service;

    public OnboardingController(OnboardingService service) {
        this.service = service;
    }

    @GetMapping
    public List<OnboardingDto> lister(@RequestParam(required = false) String statut, HttpSession session) {
        Acces.exiger(session, Acces.RH, Acces.ADMIN, Acces.DG, Acces.MANAGER);
        return service.lister(statut);
    }

    @GetMapping("/{id}")
    public OnboardingDto fiche(@PathVariable Integer id, HttpSession session) {
        return service.fiche(id, Acces.connecte(session));
    }

    @GetMapping("/mes-taches")
    public List<TacheDto> mesTaches(HttpSession session) {
        return service.mesTaches(Acces.connecte(session));
    }

    @PostMapping("/taches/{id}")
    public OnboardingDto marquer(@PathVariable Integer id, @RequestBody TacheRequest req, HttpSession session) {
        return service.marquer(id, req, Acces.connecte(session));
    }

    @GetMapping("/modeles")
    public List<ModeleOnboardingDto> modeles(HttpSession session) {
        Acces.connecte(session);
        return service.modeles();
    }

    @GetMapping("/modeles/{id}")
    public ModeleOnboardingDto modele(@PathVariable Integer id, HttpSession session) {
        Acces.connecte(session);
        return service.modele(id);
    }

    @PostMapping("/modeles")
    public ResponseEntity<ModeleOnboardingDto> creerModele(@RequestBody ModeleOnboardingRequest req, HttpSession session) {
        Acces.exiger(session, Acces.RH, Acces.ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.enregistrerModele(null, req));
    }

    @PutMapping("/modeles/{id}")
    public ModeleOnboardingDto modifierModele(@PathVariable Integer id, @RequestBody ModeleOnboardingRequest req, HttpSession session) {
        Acces.exiger(session, Acces.RH, Acces.ADMIN);
        return service.enregistrerModele(id, req);
    }
}
