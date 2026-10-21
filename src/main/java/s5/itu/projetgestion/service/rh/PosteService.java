package s5.itu.projetgestion.service.rh;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import s5.itu.projetgestion.dto.OptionDto;
import s5.itu.projetgestion.dto.PosteDemandeRequest;
import s5.itu.projetgestion.dto.PosteDto;
import s5.itu.projetgestion.model.rh.organisation.CategorieMetier;
import s5.itu.projetgestion.model.rh.organisation.EchelonMetier;
import s5.itu.projetgestion.model.rh.organisation.Metier;
import s5.itu.projetgestion.model.rh.organisation.Poste;
import s5.itu.projetgestion.model.rh.organisation.UniteOrga;
import s5.itu.projetgestion.repository.rh.CategorieMetierRepository;
import s5.itu.projetgestion.repository.rh.EchelonMetierRepository;
import s5.itu.projetgestion.repository.rh.MetierRepository;
import s5.itu.projetgestion.repository.rh.PosteRepository;
import s5.itu.projetgestion.repository.rh.UniteOrgaRepository;

@Service
public class PosteService {

    private final PosteRepository posteRepo;
    private final MetierRepository metierRepo;
    private final CategorieMetierRepository categorieRepo;
    private final EchelonMetierRepository echelonRepo;
    private final UniteOrgaRepository uniteRepo;

    public PosteService(PosteRepository posteRepo, MetierRepository metierRepo,
                        CategorieMetierRepository categorieRepo,
                        EchelonMetierRepository echelonRepo, UniteOrgaRepository uniteRepo) {
        this.posteRepo = posteRepo;
        this.metierRepo = metierRepo;
        this.categorieRepo = categorieRepo;
        this.echelonRepo = echelonRepo;
        this.uniteRepo = uniteRepo;
    }

    // ---------- Listes pour les menus déroulants ----------

    public List<OptionDto> listerMetiers() {
        return metierRepo.findAll().stream()
            .map(m -> new OptionDto(m.getId(), m.getLibelle() + " (" + m.getCategorie().getLibelle() + ")"))
            .toList();
    }

    public List<OptionDto> listerCategories() {
        return categorieRepo.findAll().stream()
            .map(c -> new OptionDto(c.getId(), c.getLibelle())).toList();
    }

    public List<OptionDto> listerEchelons() {
        return echelonRepo.findAll().stream()
            .map(e -> new OptionDto(e.getId(), e.getLibelle())).toList();
    }

    public List<OptionDto> listerUnites() {
        return uniteRepo.findAll().stream()
            .map(u -> new OptionDto(u.getId(), u.getLibelle() + " [" + u.getType() + "]")).toList();
    }

    // Candidats au poste de supérieur direct : seulement les postes ACTIF
    public List<OptionDto> listerSuperieursPossibles() {
        return posteRepo.findByEtat("ACTIF").stream()
            .map(p -> new OptionDto(p.getId(), p.getLibelle() + " - " + p.getUniteOrga().getLibelle()))
            .toList();
    }

    // ---------- Création de la demande de poste ----------

    @Transactional   // org.springframework.transaction.annotation.Transactional
    public PosteDto creerDemande(PosteDemandeRequest req) {

        // 1. Validations de base
        if (req.libelle() == null || req.libelle().isBlank())
            throw new IllegalArgumentException("Le libellé du poste est obligatoire.");
        if (req.idEchelon() == null)
            throw new IllegalArgumentException("L'échelon est obligatoire.");
        if (req.idUniteOrga() == null)
            throw new IllegalArgumentException("L'unité organisationnelle est obligatoire.");

        boolean metierExistant = req.idMetier() != null;
        boolean nouveauMetier = req.nouveauMetierLibelle() != null && !req.nouveauMetierLibelle().isBlank();

        if (metierExistant == nouveauMetier)   // les deux ou aucun
            throw new IllegalArgumentException("Choisis un métier existant OU saisis un nouveau métier.");

        // 2. Métier : existant, ou création (avec catégorie obligatoire)
        Metier metier;
        if (metierExistant) {
            metier = metierRepo.findById(req.idMetier())
                .orElseThrow(() -> new IllegalArgumentException("Métier introuvable."));
        } else {
            if (req.idCategorie() == null)
                throw new IllegalArgumentException("La catégorie est obligatoire pour un nouveau métier.");
            CategorieMetier categorie = categorieRepo.findById(req.idCategorie())
                .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable."));
            String libelleMetier = req.nouveauMetierLibelle().trim();
            if (metierRepo.existsByLibelleIgnoreCaseAndCategorieId(libelleMetier, categorie.getId()))
                throw new IllegalArgumentException("Ce métier existe déjà dans cette catégorie.");

            metier = new Metier();
            metier.setLibelle(libelleMetier);
            metier.setCategorie(categorie);
            metier = metierRepo.save(metier);
        }

        // 3. Les autres références
        EchelonMetier echelon = echelonRepo.findById(req.idEchelon())
            .orElseThrow(() -> new IllegalArgumentException("Échelon introuvable."));
        UniteOrga unite = uniteRepo.findById(req.idUniteOrga())
            .orElseThrow(() -> new IllegalArgumentException("Unité introuvable."));

        Poste superieur = null;
        if (req.idSuperieurDirect() != null) {
            superieur = posteRepo.findById(req.idSuperieurDirect())
                .orElseThrow(() -> new IllegalArgumentException("Supérieur direct introuvable."));
            if (!"ACTIF".equals(superieur.getEtat()))
                throw new IllegalArgumentException("Le supérieur direct doit être un poste ACTIF.");
        }

        // 4. Création du poste : PROPOSE + LIBRE par défaut
        Poste poste = new Poste();
        poste.setCode(genererCode());
        poste.setMetier(metier);
        poste.setEchelon(echelon);
        poste.setUniteOrga(unite);
        poste.setSuperieurDirect(superieur);
        poste.setLibelle(req.libelle().trim());
        poste.setEtat("PROPOSE");
        poste.setStatutOccupation("LIBRE");

        return PosteDto.from(posteRepo.save(poste));
    }

    // POST-001, POST-002, ... : on prend le suivant libre
    private String genererCode() {
        long n = posteRepo.count() + 1;
        String code = String.format("POST-%03d", n);
        while (posteRepo.existsByCode(code)) {
            n++;
            code = String.format("POST-%03d", n);
        }
        return code;
    }
}