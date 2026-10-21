package s5.itu.projetgestion.service.besoin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.dto.OptionDto;
import s5.itu.projetgestion.dto.besoin.BesoinResponse;
import s5.itu.projetgestion.dto.rh.BesoinDto;
import s5.itu.projetgestion.dto.rh.BesoinRequest;
import s5.itu.projetgestion.dto.rh.LignePosteRequest;
import s5.itu.projetgestion.model.User;
import s5.itu.projetgestion.model.besoin.Besoin;
import s5.itu.projetgestion.model.besoin.BesoinPoste;
import s5.itu.projetgestion.model.besoin.Motif;
import s5.itu.projetgestion.model.postes.Poste;
import s5.itu.projetgestion.model.postes.UniteOrga;
import s5.itu.projetgestion.model.validation.Validation;
import s5.itu.projetgestion.model.validation.WorkflowValidation;
import s5.itu.projetgestion.repository.besoin.BesoinRepository;
import s5.itu.projetgestion.repository.besoin.MotifRepository;
import s5.itu.projetgestion.repository.postes.BesoinPosteRepository;
import s5.itu.projetgestion.repository.postes.PosteRepository;
import s5.itu.projetgestion.repository.postes.UniteOrgaRepository;
import s5.itu.projetgestion.repository.validation.ValidationRepository;
import s5.itu.projetgestion.repository.validation.WorkflowValidationRepository;

@Service
public class BesoinService {

    private final BesoinRepository besoinRepository;
    private final BesoinPosteRepository besoinPosteRepository;
    private final ValidationRepository validationRepository;
    private final WorkflowValidationRepository workflowRepository;
    private final MotifRepository motifRepository;
    private final PosteRepository posteRepository;
    private final UniteOrgaRepository uniteRepository;

    private static final Set<String> TYPES = Set.of("CREATION", "REMPLACEMENT");

    public BesoinService(BesoinRepository besoinRepository,
            BesoinPosteRepository besoinPosteRepository,
            ValidationRepository validationRepository,
            WorkflowValidationRepository workflowRepository,
            MotifRepository motifRepository,
            PosteRepository posteRepository,
            UniteOrgaRepository uniteRepository) {
        this.besoinRepository = besoinRepository;
        this.besoinPosteRepository = besoinPosteRepository;
        this.validationRepository = validationRepository;
        this.workflowRepository = workflowRepository;
        this.motifRepository = motifRepository;
        this.posteRepository = posteRepository;
        this.uniteRepository = uniteRepository;
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
        if (req == null || req.typeBesoin() == null || !TYPES.contains(req.typeBesoin()))
            throw new IllegalArgumentException("Type de besoin invalide (CREATION ou REMPLACEMENT).");
        if (req.idUniteOrga() == null || req.idMotif() == null || req.dateSouhaitee() == null)
            throw new IllegalArgumentException("L'unité, le motif et la date souhaitée sont obligatoires.");
        if (req.dateSouhaitee().isBefore(java.time.LocalDate.now()))
            throw new IllegalArgumentException("La date souhaitée ne peut pas être dans le passé.");
        if (req.precisionMotif() != null && req.precisionMotif().length() > 100)
            throw new IllegalArgumentException("La précision du motif ne doit pas dépasser 100 caractères.");
        if (req.lignes() == null || req.lignes().isEmpty()) throw new IllegalArgumentException("Ajoute au moins un poste.");

        Map<Integer, Integer> quantites = new LinkedHashMap<>();
        for (LignePosteRequest ligne : req.lignes()) {
            if (ligne.idPoste() == null || ligne.quantite() == null || ligne.quantite() <= 0)
                throw new IllegalArgumentException("Chaque ligne doit contenir un poste et une quantité positive.");
            quantites.merge(ligne.idPoste(), ligne.quantite(), Integer::sum);
        }
        UniteOrga unite = uniteRepository.findById(req.idUniteOrga()).orElseThrow(() -> new IllegalArgumentException("Unité introuvable."));
        Motif motif = motifRepository.findById(req.idMotif()).orElseThrow(() -> new IllegalArgumentException("Motif introuvable."));
        Map<Poste, Integer> postes = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : quantites.entrySet()) {
            Poste poste = posteRepository.findById(entry.getKey()).orElseThrow(() -> new IllegalArgumentException("Poste introuvable : " + entry.getKey()));
            if (!"ACTIF".equals(poste.getEtat()) || !poste.getUniteOrga().getId().equals(unite.getId()))
                throw new IllegalArgumentException("Le poste sélectionné doit être ACTIF et appartenir à l'unité demandeuse.");
            postes.put(poste, entry.getValue());
        }
        WorkflowValidation premiereEtape = workflowRepository.findFirstByOrderByOrdreAsc()
                .orElseThrow(() -> new IllegalStateException("Le workflow de validation est vide."));

        Besoin besoin = new Besoin();
        besoin.setReference(genererReference());
        besoin.setTypeBesoin(req.typeBesoin());
        besoin.setMotif(motif);
        besoin.setUniteOrga(unite);
        besoin.setDateSouhaitee(req.dateSouhaitee());
        besoin.setUser(user);
        besoin.setEffectif(quantites.values().stream().mapToInt(Integer::intValue).sum());
        besoin.setEtatActuel("EN_ATTENTE");
        besoin.setPrecisionMotif(req.precisionMotif() == null || req.precisionMotif().isBlank() ? null : req.precisionMotif().trim());
        besoin = besoinRepository.save(besoin);
        for (Map.Entry<Poste, Integer> entry : postes.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                BesoinPoste ligne = new BesoinPoste();
                ligne.setBesoin(besoin); ligne.setPoste(entry.getKey()); ligne.setEtat("EN_ATTENTE");
                besoinPosteRepository.save(ligne);
            }
        }
        Validation validation = new Validation();
        validation.setUser(user); validation.setBesoin(besoin); validation.setWorkflowValidation(premiereEtape);
        validation.setDecision("EN_ATTENTE"); validation.setDate(java.time.LocalDateTime.now());
        validation.setCommentaire("Demande soumise");
        validationRepository.save(validation);
        String resume = postes.entrySet().stream().map(e -> e.getKey().getLibelle() + " x" + e.getValue()).collect(Collectors.joining(", "));
        return BesoinDto.from(besoin, resume);
    }

    private String genererReference() {
        String prefix = "BES-" + java.time.LocalDate.now().getYear() + "-";
        long n = besoinRepository.countByReferenceStartingWith(prefix) + 1;
        String reference = prefix + String.format("%03d", n);
        while (besoinRepository.existsByReference(reference)) reference = prefix + String.format("%03d", ++n);
        return reference;
    }

}
