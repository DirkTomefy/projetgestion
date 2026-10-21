package s5.itu.projetgestion.service.rh;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import s5.itu.projetgestion.model.User;
import org.springframework.boot.data.autoconfigure.web.DataWebProperties.Sort;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import s5.itu.projetgestion.dto.OptionDto;
import s5.itu.projetgestion.dto.rh.BesoinDto;
import s5.itu.projetgestion.dto.rh.BesoinRequest;
import s5.itu.projetgestion.dto.rh.LignePosteRequest;
import s5.itu.projetgestion.model.rh.besoin.Besoin;
import s5.itu.projetgestion.model.rh.besoin.BesoinPoste;
import s5.itu.projetgestion.model.rh.besoin.Motif;
import s5.itu.projetgestion.model.rh.besoin.Validation;
import s5.itu.projetgestion.model.rh.besoin.WorkflowValidation;
import s5.itu.projetgestion.model.rh.organisation.Poste;
import s5.itu.projetgestion.model.rh.organisation.UniteOrga;
import s5.itu.projetgestion.repository.rh.BesoinPosteRepository;
import s5.itu.projetgestion.repository.rh.BesoinRepository;
import s5.itu.projetgestion.repository.rh.MotifRepository;
import s5.itu.projetgestion.repository.rh.PosteRepository;
import s5.itu.projetgestion.repository.rh.UniteOrgaRepository;
import s5.itu.projetgestion.repository.rh.ValidationRepository;
import s5.itu.projetgestion.repository.rh.WorkflowValidationRepository;

@Service
public class BesoinService {

    private static final Set<String> TYPES = Set.of("CREATION", "REMPLACEMENT");

    private final BesoinRepository besoinRepo;
    private final BesoinPosteRepository besoinPosteRepo;
    private final ValidationRepository validationRepo;
    private final WorkflowValidationRepository workflowRepo;
    private final MotifRepository motifRepo;
    private final PosteRepository posteRepo;
    private final UniteOrgaRepository uniteRepo;

    public BesoinService(BesoinRepository besoinRepo, BesoinPosteRepository besoinPosteRepo,
                         ValidationRepository validationRepo, WorkflowValidationRepository workflowRepo,
                         MotifRepository motifRepo, PosteRepository posteRepo,
                         UniteOrgaRepository uniteRepo) {
        this.besoinRepo = besoinRepo;
        this.besoinPosteRepo = besoinPosteRepo;
        this.validationRepo = validationRepo;
        this.workflowRepo = workflowRepo;
        this.motifRepo = motifRepo;
        this.posteRepo = posteRepo;
        this.uniteRepo = uniteRepo;
    }

    // ---------- Listes du formulaire ----------

    public List<OptionDto> listerMotifs() {
        return motifRepo.findAll().stream()
            .map(m -> new OptionDto(m.getId(), m.getLibelle())).toList();
    }

    public OptionDto creerMotif(String libelle) {
        if (libelle == null || libelle.isBlank())
            throw new IllegalArgumentException("Le libellé du motif est obligatoire.");
        String l = libelle.trim();
        if (l.length() > 150)
            throw new IllegalArgumentException("Le motif ne doit pas dépasser 150 caractères.");
        if (motifRepo.existsByLibelleIgnoreCase(l))
            throw new IllegalArgumentException("Ce motif existe déjà.");
        Motif m = new Motif();
        m.setLibelle(l);
        m = motifRepo.save(m);
        return new OptionDto(m.getId(), m.getLibelle());
    }

    // Postes ACTIF de l'unité demandeuse uniquement
    public List<OptionDto> listerPostesActifs(Integer idUnite) {
        return posteRepo.findByEtatAndUniteOrgaId("ACTIF", idUnite).stream()
            .map(p -> new OptionDto(p.getId(), p.getLibelle())).toList();
    }

    

    private String posteDuBesoin(Besoin b) {
    return besoinPosteRepo.findByBesoinId(b.getId()).stream()
        .collect(Collectors.groupingBy(bp -> bp.getPoste().getLibelle(),
                 LinkedHashMap::new, Collectors.counting()))
        .entrySet().stream()
        .map(e -> e.getKey() + " x" + e.getValue())
        .collect(Collectors.joining(", "));
}

    // ---------- Enregistrement : 3 insertions, une transaction ----------

  
@Transactional
public BesoinDto creerBesoin(BesoinRequest req, User user) {

    // 1. Validations
    if (req.typeBesoin() == null || !TYPES.contains(req.typeBesoin()))
        throw new IllegalArgumentException("Type de besoin invalide (CREATION ou REMPLACEMENT).");
    if (req.idUniteOrga() == null)
        throw new IllegalArgumentException("L'unité est obligatoire.");
    if (req.idMotif() == null)
        throw new IllegalArgumentException("Le motif est obligatoire.");
    if (req.dateSouhaitee() == null)
        throw new IllegalArgumentException("La date souhaitée est obligatoire.");
    if (req.dateSouhaitee().isBefore(LocalDate.now()))
        throw new IllegalArgumentException("La date souhaitée ne peut pas être dans le passé.");
    if (req.precisionMotif() != null && req.precisionMotif().length() > 100)
        throw new IllegalArgumentException("La précision du motif ne doit pas dépasser 100 caractères.");
    if (req.lignes() == null || req.lignes().isEmpty())
        throw new IllegalArgumentException("Ajoute au moins un poste.");

    // Regroupe par poste (si le même poste apparaît deux fois, on additionne)
    Map<Integer, Integer> quantites = new LinkedHashMap<>();
    for (LignePosteRequest l : req.lignes()) {
        if (l.idPoste() == null)
            throw new IllegalArgumentException("Chaque ligne doit avoir un poste.");
        if (l.quantite() == null || l.quantite() <= 0)
            throw new IllegalArgumentException("La quantité de chaque poste doit être supérieure à 0.");
        quantites.merge(l.idPoste(), l.quantite(), Integer::sum);
    }
    int effectif = quantites.values().stream().mapToInt(Integer::intValue).sum();

    UniteOrga unite = uniteRepo.findById(req.idUniteOrga())
        .orElseThrow(() -> new IllegalArgumentException("Unité introuvable."));
    Motif motif = motifRepo.findById(req.idMotif())
        .orElseThrow(() -> new IllegalArgumentException("Motif introuvable."));

    Map<Poste, Integer> postes = new LinkedHashMap<>();
    for (Map.Entry<Integer, Integer> e : quantites.entrySet()) {
        Poste p = posteRepo.findById(e.getKey())
            .orElseThrow(() -> new IllegalArgumentException("Poste introuvable : " + e.getKey()));
        if (!"ACTIF".equals(p.getEtat()))
            throw new IllegalArgumentException("Le poste " + p.getLibelle() + " n'est pas ACTIF.");
        if (!p.getUniteOrga().getId().equals(unite.getId()))
            throw new IllegalArgumentException("Le poste " + p.getLibelle() + " n'appartient pas à l'unité demandeuse.");
        postes.put(p, e.getValue());
    }

    WorkflowValidation premiereEtape = workflowRepo.findFirstByOrderByOrdreAsc()
        .orElseThrow(() -> new IllegalStateException(
            "Le workflow de validation est vide (à remplir par P3)."));

    // 2. Insertion besoin (effectif = somme des quantités)
    Besoin besoin = new Besoin();
    besoin.setReference(genererReference());
    besoin.setTypeBesoin(req.typeBesoin());
    besoin.setMotif(motif);
    besoin.setUniteOrga(unite);
    besoin.setDateSouhaitee(req.dateSouhaitee());
    besoin.setUser(user);
    besoin.setEffectif(effectif);
    besoin.setEtatActuel("EN_ATTENTE");
    besoin.setPrecisionMotif(req.precisionMotif() == null || req.precisionMotif().isBlank()
        ? null : req.precisionMotif().trim());
    besoin = besoinRepo.save(besoin);

    // 3. besoin_poste : une ligne par personne demandée
    for (Map.Entry<Poste, Integer> e : postes.entrySet()) {
        for (int i = 0; i < e.getValue(); i++) {
            BesoinPoste bp = new BesoinPoste();
            bp.setBesoin(besoin);
            bp.setPoste(e.getKey());
            bp.setEtat("EN_ATTENTE");
            besoinPosteRepo.save(bp);
        }
    }

    // 4. Première validation
    Validation v = new Validation();
    v.setUser(user);
    v.setBesoin(besoin);
    v.setWorkflowValidation(premiereEtape);
    v.setDecision("EN_ATTENTE");
    v.setCommentaire("Demande soumise");
    validationRepo.save(v);

    String resume = postes.entrySet().stream()
        .map(e -> e.getKey().getLibelle() + " x" + e.getValue())
        .collect(Collectors.joining(", "));
    return BesoinDto.from(besoin, resume);
}
    // BES-2026-001, BES-2026-002, ...
    private String genererReference() {
        String prefix = "BES-" + LocalDate.now().getYear() + "-";
        long n = besoinRepo.countByReferenceStartingWith(prefix) + 1;
        String ref = prefix + String.format("%03d", n);
        while (besoinRepo.existsByReference(ref)) {
            n++;
            ref = prefix + String.format("%03d", n);
        }
        return ref;
    }
}