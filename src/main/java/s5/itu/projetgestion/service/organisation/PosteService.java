package s5.itu.projetgestion.service.organisation;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import s5.itu.projetgestion.dto.commun.OptionDto;
import s5.itu.projetgestion.dto.organisation.PosteDemandeRequest;
import s5.itu.projetgestion.dto.organisation.PosteDto;
import s5.itu.projetgestion.model.organisation.CategorieMetier;
import s5.itu.projetgestion.model.organisation.EchelonMetier;
import s5.itu.projetgestion.model.organisation.Metier;
import s5.itu.projetgestion.model.organisation.Poste;
import s5.itu.projetgestion.model.organisation.UniteOrga;
import s5.itu.projetgestion.repository.organisation.CategorieMetierRepository;
import s5.itu.projetgestion.repository.organisation.EchelonMetierRepository;
import s5.itu.projetgestion.repository.organisation.MetierRepository;
import s5.itu.projetgestion.repository.organisation.PosteRepository;
import s5.itu.projetgestion.repository.organisation.UniteOrgaRepository;

@Service
public class PosteService {

    private static final Set<String> ETATS = Set.of("PROPOSE", "ACTIF", "REFUSE", "SUPPRIME");

    private final PosteRepository posteRepository;
    private final MetierRepository metierRepository;
    private final CategorieMetierRepository categorieRepository;
    private final EchelonMetierRepository echelonRepository;
    private final UniteOrgaRepository uniteRepository;
    private final s5.itu.projetgestion.repository.besoin.BesoinPosteRepository besoinPosteRepository;

    public PosteService(PosteRepository posteRepository,
            MetierRepository metierRepository,
            CategorieMetierRepository categorieRepository,
            EchelonMetierRepository echelonRepository,
            UniteOrgaRepository uniteRepository,
            s5.itu.projetgestion.repository.besoin.BesoinPosteRepository besoinPosteRepository) {
        this.besoinPosteRepository = besoinPosteRepository;
        this.posteRepository = posteRepository;
        this.metierRepository = metierRepository;
        this.categorieRepository = categorieRepository;
        this.echelonRepository = echelonRepository;
        this.uniteRepository = uniteRepository;
    }

    @Transactional(readOnly = true)
    public List<PosteDto> listerParEtat(String etat) {
        if (!ETATS.contains(etat)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "État inconnu : " + etat);
        }
        return posteRepository.findByEtatAvecRelations(etat).stream()
                .map(PosteDto::from)
                .toList();
    }

    /**
     * Traite un poste PROPOSE :
     * - ACTIF  : validation
     * - REFUSE : refus (SUPPRIME est réservé à la suppression d'un poste déjà actif)
     */
    @Transactional
    public PosteDto traiterProposition(Integer id, String nouvelEtat) {
        if (!"ACTIF".equals(nouvelEtat) && !"REFUSE".equals(nouvelEtat)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Seuls les états ACTIF (validation) et REFUSE (refus) sont autorisés.");
        }

        Poste poste = posteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poste introuvable : " + id));

        if (!"PROPOSE".equals(poste.getEtat())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Le poste n'est plus à l'état PROPOSE (état actuel : " + poste.getEtat() + ").");
        }
        // un poste proposé dans un besoin est validé (ou refusé) avec ce besoin, par son circuit de validation
        if (besoinPosteRepository.existsByPosteIdAndEtatIn(id, java.util.List.of("EN_ATTENTE"))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce poste a été demandé dans un besoin : il sera activé ou refusé avec la décision sur ce besoin.");
        }

        poste.setEtat(nouvelEtat);
        return PosteDto.from(posteRepository.save(poste));
    }

 
    public List<OptionDto> listerCategories() {
        return categorieRepository.findAll().stream().map(c -> new OptionDto(c.getId(), c.getLibelle())).toList();
    }

    public List<OptionDto> listerEchelons() {
        return echelonRepository.findAll().stream().map(e -> new OptionDto(e.getId(), e.getLibelle())).toList();
    }

    public List<OptionDto> listerUnites() {
        return uniteRepository.findAll().stream().map(u -> new OptionDto(u.getId(), u.getLibelle() + " [" + u.getType() + "]")).toList();
    }

    @Transactional(readOnly = true)
    public List<OptionDto> listerMetiers() {
        return metierRepository.findAll().stream()
                .map(m -> new OptionDto(m.getId(),
                        m.getLibelle() + " (" + m.getCategorie().getLibelle() + ")"))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OptionDto> listerSuperieursPossibles() {
        return posteRepository.findByEtat("ACTIF").stream()
                .map(p -> new OptionDto(p.getId(),
                        p.getLibelle() + " - " + p.getUniteOrga().getLibelle()))
                .toList();
    }
    
    @Transactional
    public PosteDto creerDemande(PosteDemandeRequest req) {
        if (req == null || req.libelle() == null || req.libelle().isBlank())
            throw new IllegalArgumentException("Le libellé du poste est obligatoire.");
        if (req.idEchelon() == null || req.idUniteOrga() == null)
            throw new IllegalArgumentException("L'échelon et l'unité organisationnelle sont obligatoires.");
        boolean metierExistant = req.idMetier() != null;
        boolean nouveauMetier = req.nouveauMetierLibelle() != null && !req.nouveauMetierLibelle().isBlank();
        if (metierExistant == nouveauMetier)
            throw new IllegalArgumentException("Choisis un métier existant OU saisis un nouveau métier.");

        Metier metier;
        if (metierExistant) {
            metier = metierRepository.findById(req.idMetier()).orElseThrow(() -> new IllegalArgumentException("Métier introuvable."));
        } else {
            if (req.idCategorie() == null) throw new IllegalArgumentException("La catégorie est obligatoire pour un nouveau métier.");
            CategorieMetier categorie = categorieRepository.findById(req.idCategorie()).orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable."));
            String libelle = req.nouveauMetierLibelle().trim();
            if (metierRepository.existsByLibelleIgnoreCaseAndCategorieId(libelle, categorie.getId()))
                throw new IllegalArgumentException("Ce métier existe déjà dans cette catégorie.");
            metier = new Metier(); metier.setLibelle(libelle); metier.setCategorie(categorie); metier = metierRepository.save(metier);
        }
        EchelonMetier echelon = echelonRepository.findById(req.idEchelon()).orElseThrow(() -> new IllegalArgumentException("Échelon introuvable."));
        UniteOrga unite = uniteRepository.findById(req.idUniteOrga()).orElseThrow(() -> new IllegalArgumentException("Unité introuvable."));
        Poste superieur = req.idSuperieurDirect() == null ? null : posteRepository.findById(req.idSuperieurDirect())
                .orElseThrow(() -> new IllegalArgumentException("Supérieur direct introuvable."));
        if (superieur != null && !"ACTIF".equals(superieur.getEtat())) throw new IllegalArgumentException("Le supérieur direct doit être un poste ACTIF.");
        Poste poste = new Poste(); poste.setCode(genererCode()); poste.setMetier(metier); poste.setEchelon(echelon);
        poste.setUniteOrga(unite); poste.setSuperieurDirect(superieur); poste.setLibelle(req.libelle().trim());
        poste.setEtat("PROPOSE"); poste.setStatutOccupation("LIBRE");
        return PosteDto.from(posteRepository.save(poste));
    }

    private String genererCode() {
        long n = posteRepository.count() + 1;
        String code = String.format("POST-%03d", n);
        while (posteRepository.existsByCode(code)) code = String.format("POST-%03d", ++n);
        return code;
    }
}