package s5.itu.projetgestion.service.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.repository.auth.UserRepository;
import s5.itu.projetgestion.service.notification.NotificationService;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import s5.itu.projetgestion.dto.finance.GrilleDto;
import s5.itu.projetgestion.dto.finance.GrilleRequest;
import s5.itu.projetgestion.model.finance.GrilleSalariale;
import s5.itu.projetgestion.model.finance.GrilleSalarialeDetail;
import s5.itu.projetgestion.repository.finance.GrilleSalarialeDetailRepository;
import s5.itu.projetgestion.repository.finance.GrilleSalarialeRepository;
import s5.itu.projetgestion.repository.organisation.CategorieMetierRepository;
import s5.itu.projetgestion.repository.organisation.EchelonMetierRepository;

/**
 * Interface commune (P3, P4) : fourchette salariale d'un poste à une date.
 * La grille retenue est celle en vigueur à cette date (date_effectif ≤ date ≤ date_fin) ;
 * la ligne est celle de la catégorie du métier du poste et de l'échelon du poste.
 * Gère aussi les grilles (consultation, nouvelle grille qui clôt la précédente).
 */
@Service
public class GrilleSalarialeService {

    private final GrilleSalarialeRepository grilleRepo;
    private final GrilleSalarialeDetailRepository detailRepo;
    private final CategorieMetierRepository categorieRepo;
    private final EchelonMetierRepository echelonRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private static final DateTimeFormatter FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public GrilleSalarialeService(GrilleSalarialeRepository grilleRepo, GrilleSalarialeDetailRepository detailRepo,
                                  CategorieMetierRepository categorieRepo, EchelonMetierRepository echelonRepo,
                                  UserRepository userRepo, @org.springframework.context.annotation.Lazy NotificationService notificationService) {
        this.userRepo = userRepo;
        this.notificationService = notificationService;
        this.grilleRepo = grilleRepo;
        this.detailRepo = detailRepo;
        this.categorieRepo = categorieRepo;
        this.echelonRepo = echelonRepo;
    }

    /** Vide si le poste n'existe pas ou si aucune grille n'est en vigueur à cette date. */
    @Transactional(readOnly = true)
    public Optional<FourchetteSalariale> fourchette(Integer idPoste, LocalDate date) {
        if (idPoste == null) return Optional.empty();
        return grilleRepo.fourchette(idPoste, date != null ? date : LocalDate.now())
                .map(r -> new FourchetteSalariale(r.getIdPoste(), r.getCategorie(), r.getEchelon(),
                        r.getMontantMin(), r.getMontantMax(), r.getIdGrille(), r.getDateEffectif()));
    }

    // ------------------------------------------------------------------ gestion des grilles

    @Transactional(readOnly = true)
    public List<GrilleDto> lister() {
        LocalDate j = LocalDate.now();
        return grilleRepo.findAllByOrderByDateEffectifDesc().stream().map(g -> toDto(g, j, false)).toList();
    }

    @Transactional(readOnly = true)
    public GrilleDto fiche(Integer id) {
        return toDto(trouver(id), LocalDate.now(), true);
    }

    @Transactional(readOnly = true)
    public Optional<GrilleDto> enVigueur() {
        LocalDate j = LocalDate.now();
        return grilleRepo.enVigueur(j).stream().findFirst().map(g -> toDto(g, j, true));
    }

    /**
     * Nouvelle grille. Saisie par le DG : validée tout de suite. Saisie par la Finance : à valider par le DG.
     * À la validation, la grille alors en vigueur se termine la veille de la date d'effet.
     * Chaque ligne : catégorie × échelon, minimum ≤ maximum, sans doublon.
     */
    @Transactional
    public GrilleDto creer(GrilleRequest req, SessionUser su) {
        if (req == null || req.dateEffectif() == null) throw new IllegalArgumentException("La date d'effet est obligatoire.");
        if (req.lignes() == null || req.lignes().isEmpty()) throw new IllegalArgumentException("Saisissez au moins une ligne de grille.");
        for (GrilleSalariale g : grilleRepo.findAll()) {
            if (GrilleSalariale.A_VALIDER.equals(g.getStatut()))
                throw new IllegalStateException("Une grille attend déjà la décision du DG : elle doit être validée ou refusée d'abord.");
            if (GrilleSalariale.VALIDEE.equals(g.getStatut()) && !g.getDateEffectif().isBefore(req.dateEffectif()))
                throw new IllegalArgumentException("Une grille prend déjà effet le " + g.getDateEffectif() + " ou après : choisissez une date d'effet plus récente.");
        }
        GrilleSalariale g = new GrilleSalariale();
        g.setDateEffectif(req.dateEffectif());
        g.setStatut(GrilleSalariale.A_VALIDER);
        g.setProposePar(userRepo.getReferenceById(su.getId()));
        g = grilleRepo.save(g);
        Set<String> vus = new HashSet<>();
        for (GrilleRequest.Ligne l : req.lignes()) {
            if (l.idCategorie() == null || l.idEchelon() == null) throw new IllegalArgumentException("Chaque ligne doit avoir une catégorie et un échelon.");
            if (!vus.add(l.idCategorie() + ":" + l.idEchelon())) throw new IllegalArgumentException("Une même catégorie et un même échelon apparaissent deux fois.");
            if (l.montantMin() == null || l.montantMax() == null || l.montantMin().signum() <= 0 || l.montantMin().compareTo(l.montantMax()) > 0)
                throw new IllegalArgumentException("Chaque ligne doit avoir un minimum positif et inférieur ou égal au maximum.");
            GrilleSalarialeDetail d = new GrilleSalarialeDetail();
            d.setGrille(g);
            d.setCategorie(categorieRepo.findById(l.idCategorie()).orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable.")));
            d.setEchelon(echelonRepo.findById(l.idEchelon()).orElseThrow(() -> new IllegalArgumentException("Échelon introuvable.")));
            d.setMontantMin(l.montantMin().doubleValue());
            d.setMontantMax(l.montantMax().doubleValue());
            detailRepo.save(d);
        }
        if (su.hasRole("DG")) {
            appliquer(g, su);
        } else {
            notificationService.notifierRole("DG", "GRILLE_A_VALIDER", "Nouvelle grille salariale proposée (effet au "
                    + g.getDateEffectif().format(FR) + ") — à valider", "/finance/grille");
        }
        return toDto(g, LocalDate.now(), true);
    }

    @Transactional
    public GrilleDto valider(Integer id, SessionUser su) {
        GrilleSalariale g = trouver(id);
        if (!GrilleSalariale.A_VALIDER.equals(g.getStatut())) throw new IllegalStateException("Cette grille n'attend pas de validation.");
        appliquer(g, su);
        notificationService.notifierRole("Finance", "GRILLE_DECISION", "La grille salariale du " + g.getDateEffectif().format(FR)
                + " est validée par le DG", "/finance/grille");
        return toDto(g, LocalDate.now(), true);
    }

    @Transactional
    public GrilleDto refuser(Integer id, String commentaire, SessionUser su) {
        GrilleSalariale g = trouver(id);
        if (!GrilleSalariale.A_VALIDER.equals(g.getStatut())) throw new IllegalStateException("Cette grille n'attend pas de validation.");
        if (commentaire == null || commentaire.isBlank()) throw new IllegalArgumentException("Le motif du refus est obligatoire.");
        g.setStatut(GrilleSalariale.REFUSEE);
        g.setDecidePar(userRepo.getReferenceById(su.getId()));
        g.setDateDecision(LocalDateTime.now());
        g.setCommentaire(commentaire.trim());
        notificationService.notifierRole("Finance", "GRILLE_DECISION", "La grille salariale du " + g.getDateEffectif().format(FR)
                + " est refusée : " + commentaire.trim(), "/finance/grille");
        return toDto(g, LocalDate.now(), true);
    }

    @Transactional(readOnly = true)
    public long aValider() {
        return grilleRepo.countByStatut(GrilleSalariale.A_VALIDER);
    }

    /** Validation : clôt la grille en vigueur à la date d'effet, puis applique la nouvelle. */
    private void appliquer(GrilleSalariale g, SessionUser su) {
        for (GrilleSalariale x : grilleRepo.findAll()) {
            if (x.getId().equals(g.getId()) || !GrilleSalariale.VALIDEE.equals(x.getStatut())) continue;
            if (!x.getDateEffectif().isBefore(g.getDateEffectif()))
                throw new IllegalStateException("Une grille validée prend déjà effet le " + x.getDateEffectif() + " ou après.");
            if (x.getDateFin() == null || !x.getDateFin().isBefore(g.getDateEffectif())) x.setDateFin(g.getDateEffectif().minusDays(1));
        }
        g.setStatut(GrilleSalariale.VALIDEE);
        g.setDecidePar(userRepo.getReferenceById(su.getId()));
        g.setDateDecision(LocalDateTime.now());
    }

    private GrilleSalariale trouver(Integer id) {
        return grilleRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grille introuvable : " + id));
    }

    private GrilleDto toDto(GrilleSalariale g, LocalDate jour, boolean avecLignes) {
        boolean vigueur = !g.getDateEffectif().isAfter(jour) && (g.getDateFin() == null || !g.getDateFin().isBefore(jour));
        String etat = !GrilleSalariale.VALIDEE.equals(g.getStatut()) ? g.getStatut()
                : vigueur ? "EN_VIGUEUR" : g.getDateEffectif().isAfter(jour) ? "A_VENIR" : "ECHUE";
        List<GrilleDto.Ligne> lignes = !avecLignes ? List.of() : detailRepo.findParGrille(g.getId()).stream()
                .map(d -> new GrilleDto.Ligne(d.getId(), d.getCategorie().getId(), d.getCategorie().getLibelle(),
                        d.getEchelon().getId(), d.getEchelon().getLibelle(),
                        BigDecimal.valueOf(d.getMontantMin()), BigDecimal.valueOf(d.getMontantMax())))
                .toList();
        return new GrilleDto(g.getId(), g.getDateEffectif(), g.getDateFin(), etat, lignes, g.getStatut(),
                nom(g.getProposePar()), nom(g.getDecidePar()), g.getDateDecision(), g.getCommentaire());
    }

    private static String nom(s5.itu.projetgestion.model.auth.User u) {
        return u == null ? null : u.getNomComplet() != null ? u.getNomComplet() : u.getLogin();
    }
}
