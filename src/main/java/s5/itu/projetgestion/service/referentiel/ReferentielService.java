package s5.itu.projetgestion.service.referentiel;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import s5.itu.projetgestion.dto.referentiel.ReferentielDto;
import s5.itu.projetgestion.dto.referentiel.ReferentielRequest;
import s5.itu.projetgestion.model.besoin.Motif;
import s5.itu.projetgestion.model.commun.TypeDocument;
import s5.itu.projetgestion.model.contrat.TypeContrat;
import s5.itu.projetgestion.model.organisation.CategorieMetier;
import s5.itu.projetgestion.model.organisation.EchelonMetier;
import s5.itu.projetgestion.model.organisation.Metier;
import s5.itu.projetgestion.model.recrutement.TypeInformation;
import s5.itu.projetgestion.repository.besoin.MotifRepository;
import s5.itu.projetgestion.repository.commun.TypeDocumentRepository;
import s5.itu.projetgestion.repository.contrat.TypeContratRepository;
import s5.itu.projetgestion.repository.organisation.CategorieMetierRepository;
import s5.itu.projetgestion.repository.organisation.EchelonMetierRepository;
import s5.itu.projetgestion.repository.organisation.MetierRepository;
import s5.itu.projetgestion.repository.recrutement.TypeInformationRepository;

/**
 * Référentiels de l'application (listes de valeurs) : création, modification, suppression.
 * Une valeur déjà utilisée ne peut pas être supprimée ; une valeur « système » (utilisée par le code)
 * ne peut être ni renommée ni supprimée.
 */
@Service
@Transactional
public class ReferentielService {

    /** Référentiels gérés : clé d'URL → libellé affiché. */
    public static final Map<String, String> REFERENTIELS = Map.of(
            "categories", "Catégories de métier",
            "echelons", "Échelons",
            "metiers", "Métiers",
            "motifs", "Motifs de besoin",
            "types-contrat", "Types de contrat",
            "types-document", "Types de document",
            "types-information", "Types d'information du CV");

    private static final Set<String> TYPES_DOCUMENT_SYSTEME = Set.of("CV", "OFFRE", "CONTRAT", "CONTRAT_SIGNE");
    private static final Set<String> TYPES_CONTRAT_SYSTEME = Set.of("CE", "CDD", "CDI");
    private static final Set<String> TYPES_INFORMATION_SYSTEME = Set.of("EXPERIENCE_PRO", "EDUCATION", "QUALITE");

    private final CategorieMetierRepository categorieRepo;
    private final EchelonMetierRepository echelonRepo;
    private final MetierRepository metierRepo;
    private final MotifRepository motifRepo;
    private final TypeContratRepository typeContratRepo;
    private final TypeDocumentRepository typeDocumentRepo;
    private final TypeInformationRepository typeInformationRepo;

    public ReferentielService(CategorieMetierRepository categorieRepo, EchelonMetierRepository echelonRepo,
                              MetierRepository metierRepo, MotifRepository motifRepo,
                              TypeContratRepository typeContratRepo, TypeDocumentRepository typeDocumentRepo,
                              TypeInformationRepository typeInformationRepo) {
        this.categorieRepo = categorieRepo;
        this.echelonRepo = echelonRepo;
        this.metierRepo = metierRepo;
        this.motifRepo = motifRepo;
        this.typeContratRepo = typeContratRepo;
        this.typeDocumentRepo = typeDocumentRepo;
        this.typeInformationRepo = typeInformationRepo;
    }

    // ------------------------------------------------------------------ lecture

    @Transactional(readOnly = true)
    public List<ReferentielDto> lister(String ref) {
        List<ReferentielDto> l = switch (ref) {
            case "categories" -> categorieRepo.findAll().stream().map(c -> ReferentielDto.simple(c.getId(), c.getLibelle(), false)).toList();
            case "echelons" -> echelonRepo.findAll().stream().map(e -> ReferentielDto.simple(e.getId(), e.getLibelle(), false)).toList();
            case "motifs" -> motifRepo.findAll().stream().map(m -> ReferentielDto.simple(m.getId(), m.getLibelle(), false)).toList();
            case "types-document" -> typeDocumentRepo.findAll().stream()
                    .map(t -> ReferentielDto.simple(t.getId(), t.getLibelle(), TYPES_DOCUMENT_SYSTEME.contains(t.getLibelle()))).toList();
            case "types-information" -> typeInformationRepo.findAll().stream()
                    .map(t -> ReferentielDto.simple(t.getId(), t.getLibelle(), TYPES_INFORMATION_SYSTEME.contains(t.getLibelle()))).toList();
            case "metiers" -> metierRepo.findAll().stream().map(ReferentielService::metier).toList();
            case "types-contrat" -> typeContratRepo.findAll().stream().map(ReferentielService::typeContrat).toList();
            default -> throw inconnu(ref);
        };
        if ("echelons".equals(ref)) return l.stream().sorted(Comparator.comparing(ReferentielDto::id)).toList();   // ordre de progression
        return l.stream().sorted(Comparator.comparing(ReferentielDto::libelle, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    // ------------------------------------------------------------------ écriture

    public ReferentielDto creer(String ref, ReferentielRequest req) {
        return enregistrer(ref, null, req);
    }

    public ReferentielDto modifier(String ref, Integer id, ReferentielRequest req) {
        if (id == null) throw new IllegalArgumentException("Identifiant manquant.");
        return enregistrer(ref, id, req);
    }

    public void supprimer(String ref, Integer id) {
        switch (ref) {
            case "categories" -> supprimer(categorieRepo, id, c -> false);
            case "echelons" -> supprimer(echelonRepo, id, e -> false);
            case "motifs" -> supprimer(motifRepo, id, m -> false);
            case "metiers" -> supprimer(metierRepo, id, m -> false);
            case "types-document" -> supprimer(typeDocumentRepo, id, t -> TYPES_DOCUMENT_SYSTEME.contains(t.getLibelle()));
            case "types-information" -> supprimer(typeInformationRepo, id, t -> TYPES_INFORMATION_SYSTEME.contains(t.getLibelle()));
            case "types-contrat" -> supprimer(typeContratRepo, id, t -> TYPES_CONTRAT_SYSTEME.contains(t.getLibelle()));
            default -> throw inconnu(ref);
        }
    }

    private ReferentielDto enregistrer(String ref, Integer id, ReferentielRequest req) {
        if (req == null || req.libelle() == null || req.libelle().isBlank())
            throw new IllegalArgumentException("Le libellé est obligatoire.");
        String libelle = req.libelle().trim();
        if (libelle.length() > 150) throw new IllegalArgumentException("Le libellé ne doit pas dépasser 150 caractères.");
        return switch (ref) {
            case "categories" -> {
                CategorieMetier c = simple(categorieRepo, id, CategorieMetier::new, CategorieMetier::getLibelle, CategorieMetier::setLibelle, libelle, x -> false);
                yield ReferentielDto.simple(c.getId(), c.getLibelle(), false);
            }
            case "echelons" -> {
                EchelonMetier e = simple(echelonRepo, id, EchelonMetier::new, EchelonMetier::getLibelle, EchelonMetier::setLibelle, libelle, x -> false);
                yield ReferentielDto.simple(e.getId(), e.getLibelle(), false);
            }
            case "motifs" -> {
                Motif m = simple(motifRepo, id, Motif::new, Motif::getLibelle, Motif::setLibelle, libelle, x -> false);
                yield ReferentielDto.simple(m.getId(), m.getLibelle(), false);
            }
            case "types-document" -> {
                String code = code(libelle);
                TypeDocument t = simple(typeDocumentRepo, id, TypeDocument::new, TypeDocument::getLibelle, TypeDocument::setLibelle, code,
                        x -> TYPES_DOCUMENT_SYSTEME.contains(x.getLibelle()));
                yield ReferentielDto.simple(t.getId(), t.getLibelle(), TYPES_DOCUMENT_SYSTEME.contains(t.getLibelle()));
            }
            case "types-information" -> {
                String code = code(libelle);
                if (code.length() > 30) throw new IllegalArgumentException("Le code ne doit pas dépasser 30 caractères.");
                TypeInformation t = simple(typeInformationRepo, id, TypeInformation::new, TypeInformation::getLibelle, TypeInformation::setLibelle, code,
                        x -> TYPES_INFORMATION_SYSTEME.contains(x.getLibelle()));
                yield ReferentielDto.simple(t.getId(), t.getLibelle(), TYPES_INFORMATION_SYSTEME.contains(t.getLibelle()));
            }
            case "metiers" -> {
                if (req.idParent() == null) throw new IllegalArgumentException("La catégorie du métier est obligatoire.");
                CategorieMetier cat = categorieRepo.findById(req.idParent())
                        .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable."));
                Metier m = id == null ? new Metier() : metierRepo.findById(id).orElseThrow(() -> introuvable(id));
                boolean doublon = metierRepo.findAll().stream().anyMatch(x -> !x.getId().equals(m.getId())
                        && x.getLibelle().equalsIgnoreCase(libelle) && x.getCategorie().getId().equals(cat.getId()));
                if (doublon) throw new IllegalArgumentException("Ce métier existe déjà dans cette catégorie.");
                m.setLibelle(libelle);
                m.setCategorie(cat);
                yield metier(metierRepo.save(m));
            }
            case "types-contrat" -> {
                String code = code(libelle);
                TypeContrat t = id == null ? new TypeContrat() : typeContratRepo.findById(id).orElseThrow(() -> introuvable(id));
                if (t.getId() != null && TYPES_CONTRAT_SYSTEME.contains(t.getLibelle()) && !t.getLibelle().equals(code))
                    throw new IllegalStateException("Le type " + t.getLibelle() + " est utilisé par l'application : il ne peut pas être renommé.");
                typeContratRepo.findByLibelleIgnoreCase(code).filter(x -> !x.getId().equals(t.getId()))
                        .ifPresent(x -> { throw new IllegalArgumentException("Ce type de contrat existe déjà."); });
                if (req.dureeMax() != null && req.dureeMax() <= 0) throw new IllegalArgumentException("La durée maximale doit être positive (en mois).");
                if (req.renouvellementMax() != null && req.renouvellementMax() < 0) throw new IllegalArgumentException("Le nombre de renouvellements ne peut pas être négatif.");
                t.setLibelle(code);
                t.setDureeMax(req.dureeMax());
                t.setRenouvellementMax(req.renouvellementMax());
                yield typeContrat(typeContratRepo.save(t));
            }
            default -> throw inconnu(ref);
        };
    }

    // ------------------------------------------------------------------ outils génériques

    private <T> T simple(JpaRepository<T, Integer> repo, Integer id, Supplier<T> nouveau, Function<T, String> lire,
                         BiConsumer<T, String> ecrire, String libelle, java.util.function.Predicate<T> systeme) {
        T entite = id == null ? nouveau.get() : repo.findById(id).orElseThrow(() -> introuvable(id));
        if (id != null && systeme.test(entite) && !lire.apply(entite).equals(libelle))
            throw new IllegalStateException("« " + lire.apply(entite) + " » est utilisé par l'application : il ne peut pas être renommé.");
        boolean doublon = repo.findAll().stream().anyMatch(x -> x != entite && lire.apply(x).equalsIgnoreCase(libelle));
        if (doublon) throw new IllegalArgumentException("« " + libelle + " » existe déjà.");
        ecrire.accept(entite, libelle);
        return repo.save(entite);
    }

    private <T> void supprimer(JpaRepository<T, Integer> repo, Integer id, java.util.function.Predicate<T> systeme) {
        T entite = repo.findById(id).orElseThrow(() -> introuvable(id));
        if (systeme.test(entite)) throw new IllegalStateException("Cette valeur est utilisée par l'application : elle ne peut pas être supprimée.");
        try {
            repo.delete(entite);
            repo.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Cette valeur est déjà utilisée : elle ne peut pas être supprimée.");
        }
    }

    /** Codes techniques (types de document, de contrat, d'information) : majuscules, sans espaces. */
    private static String code(String libelle) {
        return java.text.Normalizer.normalize(libelle, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .trim().toUpperCase().replaceAll("[^A-Z0-9]+", "_").replaceAll("^_|_$", "");
    }

    private static ReferentielDto metier(Metier m) {
        return new ReferentielDto(m.getId(), m.getLibelle(), m.getCategorie().getId(), m.getCategorie().getLibelle(), null, null, false);
    }

    private static ReferentielDto typeContrat(TypeContrat t) {
        return new ReferentielDto(t.getId(), t.getLibelle(), null, null, t.getDureeMax(), t.getRenouvellementMax(),
                TYPES_CONTRAT_SYSTEME.contains(t.getLibelle()));
    }

    private static ResponseStatusException introuvable(Integer id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Élément introuvable : " + id);
    }

    private static IllegalArgumentException inconnu(String ref) {
        return new IllegalArgumentException("Référentiel inconnu : " + ref);
    }
}
