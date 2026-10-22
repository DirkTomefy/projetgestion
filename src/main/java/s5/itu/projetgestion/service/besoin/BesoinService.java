package s5.itu.projetgestion.service.besoin;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import s5.itu.projetgestion.dto.besoin.Place;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.dto.commun.OptionDto;
import s5.itu.projetgestion.dto.organisation.PosteOccupeDto;
import s5.itu.projetgestion.dto.besoin.BesoinResponse;
import s5.itu.projetgestion.dto.besoin.BesoinDto;
import s5.itu.projetgestion.dto.besoin.BesoinRequest;
import s5.itu.projetgestion.dto.besoin.LignePosteRequest;
import s5.itu.projetgestion.model.auth.User;
import s5.itu.projetgestion.model.besoin.Besoin;
import s5.itu.projetgestion.model.besoin.BesoinPoste;
import s5.itu.projetgestion.model.besoin.Motif;
import s5.itu.projetgestion.model.besoin.Validation;
import s5.itu.projetgestion.model.besoin.WorkflowValidation;
import s5.itu.projetgestion.model.contrat.Contrat;
import s5.itu.projetgestion.model.organisation.Affectation;
import s5.itu.projetgestion.model.organisation.Employe;
import s5.itu.projetgestion.model.organisation.Poste;
import s5.itu.projetgestion.model.organisation.UniteOrga;
import s5.itu.projetgestion.repository.besoin.BesoinPosteRepository;
import s5.itu.projetgestion.repository.besoin.BesoinRepository;
import s5.itu.projetgestion.repository.besoin.MotifRepository;
import s5.itu.projetgestion.repository.besoin.ValidationRepository;
import s5.itu.projetgestion.repository.besoin.WorkflowValidationRepository;
import s5.itu.projetgestion.repository.contrat.ContratRepository;
import s5.itu.projetgestion.repository.organisation.AffectationRepository;
import s5.itu.projetgestion.repository.organisation.EmployeRepository;
import s5.itu.projetgestion.repository.organisation.PosteRepository;
import s5.itu.projetgestion.repository.organisation.UniteOrgaRepository;

@Service
public class BesoinService {

    private final BesoinRepository besoinRepository;
    private final BesoinPosteRepository besoinPosteRepository;
    private final ValidationRepository validationRepository;
    private final WorkflowValidationRepository workflowRepository;
    private final MotifRepository motifRepository;
    private final PosteRepository posteRepository;
    private final UniteOrgaRepository uniteRepository;
    private final ContratRepository contratRepo ;
    private final AffectationRepository affectationRepo ;
    private final EmployeRepository employeRepository ;

    private static final Set<String> TYPES = Set.of("CREATION", "REMPLACEMENT");

    public BesoinService(BesoinRepository besoinRepository,
            BesoinPosteRepository besoinPosteRepository,
            ValidationRepository validationRepository,
            WorkflowValidationRepository workflowRepository,
            MotifRepository motifRepository,
            PosteRepository posteRepository,
            UniteOrgaRepository uniteRepository , ContratRepository contratRepo , AffectationRepository affectationRepo, EmployeRepository employeRepository) {
        this.besoinRepository = besoinRepository;
        this.besoinPosteRepository = besoinPosteRepository;
        this.validationRepository = validationRepository;
        this.workflowRepository = workflowRepository;
        this.motifRepository = motifRepository;
        this.posteRepository = posteRepository;
        this.uniteRepository = uniteRepository;
        this.contratRepo = contratRepo ; 
        this.affectationRepo = affectationRepo ;
        this.employeRepository = employeRepository ;
    }

    @Transactional(readOnly = true)
    public List<BesoinResponse> findAll(){
        List<BesoinResponse> retour = new ArrayList<>();
        for(Besoin besoin : this.besoinRepository.findAll()){
            retour.add(new BesoinResponse().fromBesoin(besoin));
        }
        return retour;
    }

    public Besoin getById(Integer id) throws Exception {
        Besoin besoin = besoinRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new Exception("Besoin Introuvable"));
                return besoin;
    }

    @Transactional(readOnly = true)
    public BesoinResponse fiche(Integer id) throws Exception {
        Besoin besoin = besoinRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new Exception("Besoin Introuvable"));

        return new BesoinResponse().fromBesoin(besoin);
    }

    public List<OptionDto> listerMotifs() {
        return motifRepository.findAll().stream()
                .map(m -> new OptionDto(m.getId(), m.getLibelle())).toList();
    }

    public OptionDto creerMotif(String libelle) {
        if (libelle == null || libelle.isBlank()) throw new IllegalArgumentException("Le libellé du motif est obligatoire.");
        String valeur = libelle.trim();
        if (valeur.length() > 150) throw new IllegalArgumentException("Le motif ne doit pas dépasser 150 caractères.");
        if (motifRepository.existsByLibelleIgnoreCase(valeur)) throw new IllegalArgumentException("Ce motif existe déjà.");
        Motif motif = new Motif();
        motif.setLibelle(valeur);
        motif = motifRepository.save(motif);
        return new OptionDto(motif.getId(), motif.getLibelle());
    }

    public List<OptionDto> listerPostesActifs(Integer idUnite) {
        return posteRepository.findByEtatAndUniteOrgaId("ACTIF", idUnite).stream()
                .map(p -> new OptionDto(p.getId(), p.getLibelle())).toList();
    }

    @Transactional
public BesoinDto creerBesoin(BesoinRequest req, User user) {

    // 1. Validations générales
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

    UniteOrga unite = uniteRepository.findById(req.idUniteOrga())
        .orElseThrow(() -> new IllegalArgumentException("Unité introuvable."));
    Motif motif = motifRepository.findById(req.idMotif())
        .orElseThrow(() -> new IllegalArgumentException("Motif introuvable."));

    boolean remplacement = "REMPLACEMENT".equals(req.typeBesoin());

    // 2. Construction des places, une par personne demandée
    List<Place> places = new ArrayList<>();
    Set<String> dejaVus = new HashSet<>();

    for (LignePosteRequest l : req.lignes()) {
        if (l.idPoste() == null)
            throw new IllegalArgumentException("Chaque ligne doit avoir un poste.");
        Poste p = posteRepository.findById(l.idPoste())
            .orElseThrow(() -> new IllegalArgumentException("Poste introuvable : " + l.idPoste()));
        if (!"ACTIF".equals(p.getEtat()))
            throw new IllegalArgumentException("Le poste " + p.getLibelle() + " n'est pas ACTIF.");
        if (!p.getUniteOrga().getId().equals(unite.getId()))
            throw new IllegalArgumentException("Le poste " + p.getLibelle() + " n'appartient pas à l'unité demandeuse.");

        if (l.idEmployeRemplace() != null) {
            // --- Ligne de remplacement : poste occupé, un seul employé ---
            if (!remplacement)
                throw new IllegalArgumentException("Un employé remplacé n'est possible que pour un besoin de REMPLACEMENT.");
            if (l.quantite() != null && l.quantite() != 1)
                throw new IllegalArgumentException("Un remplacement concerne une seule personne par ligne.");
            if (!"OCCUPE".equals(p.getStatutOccupation()))
                throw new IllegalArgumentException("Le poste " + p.getLibelle() + " n'est pas occupé.");
            Employe e = employeRepository.findById(l.idEmployeRemplace())
                .orElseThrow(() -> new IllegalArgumentException("Employé introuvable."));
            if (affectationRepo.findEnCours(p.getId(), e.getId()).isEmpty())
                throw new IllegalArgumentException(e.getNom() + " n'occupe pas le poste " + p.getLibelle() + ".");
            if (!dejaVus.add(p.getId() + ":" + e.getId()))
                throw new IllegalArgumentException("Le même remplacement est demandé deux fois.");
            places.add(new Place(p, e));
        } else {
            // --- Ligne normale : poste libre, quantité >= 1 ---
            if (!"LIBRE".equals(p.getStatutOccupation()))
                throw new IllegalArgumentException("Le poste " + p.getLibelle()
                    + " est occupé : choisis un remplacement pour désigner l'employé qui part.");
            if (l.quantite() == null || l.quantite() <= 0)
                throw new IllegalArgumentException("La quantité de chaque poste doit être supérieure à 0.");
            if (!dejaVus.add(p.getId() + ":libre"))
                throw new IllegalArgumentException("Le poste " + p.getLibelle() + " apparaît deux fois.");
            for (int i = 0; i < l.quantite(); i++) places.add(new Place(p, null));
        }
    }

    WorkflowValidation premiereEtape = workflowRepository.findFirstByOrderByOrdreAsc()
        .orElseThrow(() -> new IllegalStateException(
            "Le workflow de validation est vide (à remplir par P3)."));

    // 3. Insertion besoin (effectif = nombre de places)
    Besoin besoin = new Besoin();
    besoin.setReference(genererReference());
    besoin.setTypeBesoin(req.typeBesoin());
    besoin.setMotif(motif);
    besoin.setUniteOrga(unite);
    besoin.setDateSouhaitee(req.dateSouhaitee());
    besoin.setUser(user);
    besoin.setEffectif(places.size());
    besoin.setEtatActuel("EN_ATTENTE");
    besoin.setPrecisionMotif(req.precisionMotif() == null || req.precisionMotif().isBlank()
        ? null : req.precisionMotif().trim());
    besoin = besoinRepository.save(besoin);

    // 4. besoin_poste : une ligne par place. Le poste occupé reste OCCUPE.
    for (Place pl : places) {
        BesoinPoste bp = new BesoinPoste();
        bp.setBesoin(besoin);
        bp.setPoste(pl.poste());
        bp.setEtat("EN_ATTENTE");
        bp.setEmployeRemplace(pl.remplace());
        besoinPosteRepository.save(bp);
    }

    // 5. Première validation
    Validation v = new Validation();
    v.setUser(user);
    v.setBesoin(besoin);
    v.setWorkflowValidation(premiereEtape);
    v.setDecision("EN_ATTENTE");
    v.setCommentaire("Demande soumise");
    validationRepository.save(v);

    String resume = places.stream()
        .map(pl -> pl.poste().getLibelle()
            + (pl.remplace() != null ? " (remplace " + pl.remplace().getNom() + ")" : ""))
        .collect(Collectors.joining(", "));
    return BesoinDto.from(besoin, resume);
}

    @Transactional(readOnly = true)
    public List<PosteOccupeDto> listerPostesOccupes(Integer idUnite) {
        if (idUnite == null)
            throw new IllegalArgumentException("L'unité est obligatoire.");

        return affectationRepo.findOccupees(idUnite).stream().map(a -> {
            Employe e = a.getEmploye();
            return new PosteOccupeDto(
                a.getPoste().getId(),
                a.getPoste().getLibelle(),
                e.getId(),
                e.getNom() + (e.getPrenoms() == null ? "" : " " + e.getPrenoms()),
                e.getMatricule(),
                dateDepart(a));
        }).toList();
    }

    // Date de départ : fin d'affectation, sinon fin prévue du contrat SIGNE en cours, sinon null
    private LocalDate dateDepart(Affectation a) {
        if (a.getDateFin() != null) return a.getDateFin();
        return contratRepo
            .findByAffectationIdAndStatutActuelOrderByDateDebutDesc(a.getId(), "SIGNE").stream()
            .map(Contrat::getDateFinPrevue)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }
    private String genererReference() {
        String prefix = "BES-" + java.time.LocalDate.now().getYear() + "-";
        long n = besoinRepository.countByReferenceStartingWith(prefix) + 1;
        String reference = prefix + String.format("%03d", n);
        while (besoinRepository.existsByReference(reference)) reference = prefix + String.format("%03d", ++n);
        return reference;
    }

}
