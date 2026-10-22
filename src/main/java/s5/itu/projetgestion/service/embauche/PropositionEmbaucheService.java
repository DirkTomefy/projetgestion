package s5.itu.projetgestion.service.embauche;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import s5.itu.projetgestion.dto.auth.SessionUser;
import s5.itu.projetgestion.dto.commun.OptionDto;
import s5.itu.projetgestion.dto.embauche.*;
import s5.itu.projetgestion.err.AccesRefuseException;
import s5.itu.projetgestion.err.PropositionException;
import s5.itu.projetgestion.model.besoin.BesoinPoste;
import s5.itu.projetgestion.model.contrat.TypeContrat;
import s5.itu.projetgestion.model.embauche.PointCondition;
import s5.itu.projetgestion.model.embauche.PropositionCondition;
import s5.itu.projetgestion.model.embauche.PropositionEmbauche;
import s5.itu.projetgestion.model.embauche.RelanceCondition;
import s5.itu.projetgestion.model.recrutement.Candidat;
import s5.itu.projetgestion.model.recrutement.Candidature;
import s5.itu.projetgestion.model.recrutement.OffreEmploi;
import s5.itu.projetgestion.repository.besoin.BesoinPosteRepository;
import s5.itu.projetgestion.repository.contrat.TypeContratRepository;
import s5.itu.projetgestion.repository.embauche.PointConditionRepository;
import s5.itu.projetgestion.repository.embauche.PropositionConditionRepository;
import s5.itu.projetgestion.repository.embauche.PropositionEmbaucheRepository;
import s5.itu.projetgestion.repository.embauche.RelanceConditionRepository;
import s5.itu.projetgestion.repository.recrutement.CandidatureRepository;
import s5.itu.projetgestion.repository.recrutement.OffreEmploiRepository;
import s5.itu.projetgestion.service.finance.FourchetteSalariale;
import s5.itu.projetgestion.service.finance.GrilleSalarialeService;
import s5.itu.projetgestion.service.notification.CommunicationCandidatService;
import s5.itu.projetgestion.service.recrutement.OffreEmploiService;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Proposition d'embauche, côté RH (bloc 3) : saisie à partir d'une candidature RETENUE par la sélection (P5),
 * contrôle du salaire dans la grille, contre-proposition après un refus, retrait.
 * La réponse du candidat est dans PropositionReponseService.
 */
@Service
@Transactional
public class PropositionEmbaucheService {

    /** Statuts qui bloquent une nouvelle proposition sur la même candidature ou le même siège. */
    public static final List<String> STATUTS_EN_COURS =
            List.of(PropositionEmbauche.STATUT_PROPOSER, PropositionEmbauche.STATUT_ACCEPTER);
    public static final List<String> STATUTS = List.of(PropositionEmbauche.STATUT_PROPOSER,
            PropositionEmbauche.STATUT_ACCEPTER, PropositionEmbauche.STATUT_REFUSER, PropositionEmbauche.STATUT_EXPIREE);
    public static final String TYPE_CONTRAT_PAR_DEFAUT = "CE";
    /** Sièges qui ne peuvent plus recevoir de proposition. */
    private static final Set<String> ETATS_SIEGE_FERMES = Set.of("POURVU", "ANNULE");

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PropositionEmbaucheRepository propositionRepo;
    private final PropositionConditionRepository conditionRepo;
    private final RelanceConditionRepository relanceRepo;
    private final PointConditionRepository pointRepo;
    private final CandidatureRepository candidatureRepo;
    private final OffreEmploiRepository offreRepo;
    private final BesoinPosteRepository besoinPosteRepo;
    private final TypeContratRepository typeContratRepo;
    private final GrilleSalarialeService grilleService;
    private final HistoPropositionService histoService;
    private final PropositionMapper mapper;
    private final CommunicationCandidatService communication;
    private final String frontUrl;

    public PropositionEmbaucheService(PropositionEmbaucheRepository propositionRepo,
                                      PropositionConditionRepository conditionRepo,
                                      RelanceConditionRepository relanceRepo,
                                      PointConditionRepository pointRepo,
                                      CandidatureRepository candidatureRepo,
                                      OffreEmploiRepository offreRepo,
                                      BesoinPosteRepository besoinPosteRepo,
                                      TypeContratRepository typeContratRepo,
                                      GrilleSalarialeService grilleService,
                                      HistoPropositionService histoService,
                                      PropositionMapper mapper,
                                      CommunicationCandidatService communication,
                                      @Value("${app.front.url:http://localhost:5173}") String frontUrl) {
        this.propositionRepo = propositionRepo;
        this.conditionRepo = conditionRepo;
        this.relanceRepo = relanceRepo;
        this.pointRepo = pointRepo;
        this.candidatureRepo = candidatureRepo;
        this.offreRepo = offreRepo;
        this.besoinPosteRepo = besoinPosteRepo;
        this.typeContratRepo = typeContratRepo;
        this.grilleService = grilleService;
        this.histoService = histoService;
        this.mapper = mapper;
        this.communication = communication;
        this.frontUrl = frontUrl.endsWith("/") ? frontUrl.substring(0, frontUrl.length() - 1) : frontUrl;
    }

    // ------------------------------------------------------------------ lecture

    @Transactional(readOnly = true)
    public List<PropositionResumeDto> lister(String statut) {
        String filtre = statut == null || statut.isBlank() ? null : statut.trim().toUpperCase(Locale.ROOT);
        if (filtre != null && !STATUTS.contains(filtre)) {
            throw PropositionException.invalide("statut", "Statut de proposition inconnu : " + statut);
        }
        return propositionRepo.findResumes(filtre).stream()
                .map(r -> new PropositionResumeDto(r.getId(), r.getStatut(),
                        PropositionMapper.etape(r.getStatut(), r.getNbEnAttente()),
                        r.getDateLimiteReponse(), r.getDateEnvoi(), r.getDateReponse(),
                        r.getIdCandidature(), r.getNom(), r.getPrenom(), r.getEmail(),
                        r.getIdOffre(), r.getReferenceOffre(), r.getPoste(),
                        r.getSalaire() == null ? null : ConditionsProposition.afficher(PointCondition.SALAIRE, r.getSalaire()),
                        r.getNbEnAttente().intValue(), r.getNbRefusees().intValue(), r.getIdContrat()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PropositionDto fiche(Integer idProposition) {
        return toDto(trouver(idProposition));
    }

    /** Candidature, sièges possibles (avec leur fourchette salariale) et valeurs par défaut. */
    @Transactional(readOnly = true)
    public FormulairePropositionDto formulaire(Integer idCandidature) {
        Candidature c = candidatureRetenue(idCandidature);
        OffreEmploi offre = offreDe(c);
        List<BesoinPoste> sieges = propositionRepo.findSiegesDuBesoin(offre.getIdBesoin());

        LocalDate aujourdhui = LocalDate.now();
        LocalDate dateLimite = aujourdhui.plusDays(7);
        LocalDate souhaitee = sieges.isEmpty() ? null : sieges.get(0).getBesoin().getDateSouhaitee();
        LocalDate dateDebut = souhaitee != null && !souhaitee.isBefore(dateLimite) ? souhaitee : aujourdhui.plusDays(14);

        List<SiegeDto> siegesDto = sieges.stream().map(bp -> {
            String raison = raisonIndisponible(bp);
            FourchetteSalariale f = grilleService.fourchette(bp.getPoste().getId(), dateDebut).orElse(null);
            return PropositionMapper.siege(bp, f, raison == null, raison);
        }).toList();

        TypeContrat typeDefaut = typeContratParDefaut(offre);
        List<OptionDto> types = typeContratRepo.findAll().stream()
                .sorted(Comparator.comparing(TypeContrat::getId))
                .map(t -> new OptionDto(t.getId(), t.getLibelle())).toList();
        List<PropositionResumeDto> precedentes = lister(null).stream()
                .filter(r -> r.idCandidature().equals(idCandidature)).toList();

        Candidat ca = c.getCandidat();
        return new FormulairePropositionDto(c.getId(), ca.getNom(), ca.getPrenom(), ca.getEmail(),
                offre.getId(), offre.getRef(), siegesDto, types,
                typeDefaut != null ? typeDefaut.getId() : null, dateDebut, dateLimite,
                siegesDto.isEmpty() ? null : siegesDto.get(0).unite(), precedentes);
    }

    // ------------------------------------------------------------------ création

    public PropositionDto creer(PropositionRequest req, SessionUser user) {
        Integer idUser = verifierRh(user);
        Candidature c = candidatureRetenue(req.idCandidature());
        OffreEmploi offre = offreDe(c);

        BesoinPoste siege = besoinPosteRepo.findById(req.idBesoinPoste())
                .orElseThrow(() -> PropositionException.introuvable("Poste (siège) introuvable : " + req.idBesoinPoste()));
        if (!Objects.equals(siege.getBesoin().getId(), offre.getIdBesoin())) {
            throw PropositionException.invalide("idBesoinPoste",
                    "Ce poste n'appartient pas au besoin de l'offre " + offre.getRef());
        }
        String raison = raisonIndisponible(siege);
        if (raison != null) throw new PropositionException(409, raison, "idBesoinPoste");

        TypeContrat type = req.idTypeContrat() != null
                ? typeContratRepo.findById(req.idTypeContrat())
                    .orElseThrow(() -> PropositionException.invalide("idTypeContrat", "Type de contrat introuvable"))
                : typeContratParDefaut(offre);
        if (type == null) {
            throw PropositionException.invalide("idTypeContrat",
                    "Type de contrat par défaut (" + TYPE_CONTRAT_PAR_DEFAUT + ") absent : exécuter type_contrat_data.sql");
        }

        Map<String, String> valeurs = new LinkedHashMap<>();
        valeurs.put(PointCondition.SALAIRE, ConditionsProposition.stockerSalaire(req.salaire()));
        valeurs.put(PointCondition.DATE_DEBUT, req.dateDebut().toString());
        valeurs.put(PointCondition.TYPE_CONTRAT, String.valueOf(type.getId()));   // id lu par l'embauche (P3)
        valeurs.put(PointCondition.HORAIRE, ConditionsProposition.texte(PointCondition.HORAIRE, req.horaire(), true));
        valeurs.put(PointCondition.LIEU, ConditionsProposition.texte(PointCondition.LIEU, req.lieu(), true));
        String conditions = ConditionsProposition.texte(PointCondition.CONDITIONS, req.conditionsTravail(), false);
        if (conditions != null) valeurs.put(PointCondition.CONDITIONS, conditions);

        verifierCoherence(siege, req.salaire(), req.dateDebut(), req.dateLimiteReponse());

        PropositionEmbauche p = new PropositionEmbauche();
        p.setCandidature(c);
        p.setBesoinPoste(siege);
        p.setStatutActuel(PropositionEmbauche.STATUT_PROPOSER);
        p.setDateLimiteReponse(req.dateLimiteReponse());
        p.setJeton(nouveauJeton());
        p.setDateEnvoi(LocalDateTime.now());
        p = propositionRepo.save(p);

        for (Map.Entry<String, String> e : valeurs.entrySet()) {
            PropositionCondition pc = new PropositionCondition();
            pc.setProposition(p);
            pc.setPointCondition(point(e.getKey()));
            pc.setValeur(e.getValue());
            conditionRepo.save(pc);
        }

        histoService.enregistrer(p, idUser, joindre("Proposition envoyée au candidat (réponse attendue avant le "
                + ConditionsProposition.date(p.getDateLimiteReponse()) + ")", req.commentaire()));
        communication.propositionEnvoyee(p, false);   // email avec le lien de réponse
        return toDto(p);
    }

    // ------------------------------------------------------------------ contre-proposition

    /**
     * Après un refus partiel du candidat : une nouvelle valeur pour chaque condition refusée.
     * L'ancienne valeur est gardée dans relance_condition ; la condition repasse en attente de réponse.
     */
    public PropositionDto contreProposer(Integer idProposition, ContrePropositionRequest req, SessionUser user) {
        Integer idUser = verifierRh(user);
        PropositionEmbauche p = propositionRepo.findByIdPourModification(idProposition)
                .orElseThrow(() -> PropositionException.introuvable("Proposition introuvable : " + idProposition));
        List<PropositionCondition> conditions = mapper.conditionsTriees(p.getId());
        if (!PropositionMapper.ETAPE_ATTENTE_RH.equals(PropositionMapper.etape(p.getStatutActuel(), conditions))) {
            throw PropositionException.conflit(
                    "Une contre-proposition n'est possible qu'après un refus du candidat (statut : "
                            + p.getStatutActuel() + ")");
        }

        Map<Integer, PropositionCondition> refusees = conditions.stream()
                .filter(PropositionCondition::estRefusee)
                .collect(Collectors.toMap(PropositionCondition::getId, x -> x));
        Map<Integer, String> nouvelles = new LinkedHashMap<>();
        for (ContrePropositionRequest.NouvelleValeur nv : req.conditions()) {
            if (!refusees.containsKey(nv.idCondition())) {
                throw PropositionException.invalide("conditions",
                        "Seules les conditions refusées par le candidat peuvent être modifiées");
            }
            if (nouvelles.put(nv.idCondition(), nv.valeur()) != null) {
                throw PropositionException.invalide("conditions", "Condition indiquée deux fois");
            }
        }
        if (!nouvelles.keySet().equals(refusees.keySet())) {
            String manquantes = refusees.values().stream().filter(x -> !nouvelles.containsKey(x.getId()))
                    .map(x -> ConditionsProposition.libelle(x.getCode())).collect(Collectors.joining(", "));
            throw PropositionException.invalide("conditions",
                    "Indiquez une valeur pour chaque condition refusée (manquant : " + manquantes
                            + "), ou retirez la proposition");
        }

        LocalDate dateLimite = req.dateLimiteReponse() != null ? req.dateLimiteReponse() : p.getDateLimiteReponse();
        List<String> changements = new ArrayList<>();
        for (Map.Entry<Integer, String> e : nouvelles.entrySet()) {
            PropositionCondition pc = refusees.get(e.getKey());
            String nouvelle = normaliser(pc.getCode(), e.getValue());

            RelanceCondition r = new RelanceCondition();
            r.setPropositionCondition(pc);
            r.setAncienneValeur(pc.getValeur());
            r.setNouvelleValeur(nouvelle);
            r.setStatut(RelanceCondition.STATUT_EN_ATTENTE);
            r.setIdUser(idUser);
            r.setDateRelance(LocalDateTime.now());
            r.setCommentaire(blankToNull(req.commentaire()));
            relanceRepo.save(r);

            changements.add(ConditionsProposition.libelle(pc.getCode()) + " : "
                    + mapper.afficher(pc.getCode(), pc.getValeur()) + " → "
                    + mapper.afficher(pc.getCode(), nouvelle));
            pc.setValeur(nouvelle);
            pc.setAccepte(null);
            pc.setCommentaireCandidat(null);
        }

        verifierCoherence(p.getBesoinPoste(),
                ConditionsProposition.lireSalaire(valeur(conditions, PointCondition.SALAIRE)),
                ConditionsProposition.lireDate(valeur(conditions, PointCondition.DATE_DEBUT)),
                dateLimite);

        p.setDateLimiteReponse(dateLimite);
        p.setDateEnvoi(LocalDateTime.now());
        propositionRepo.save(p);
        histoService.enregistrer(p, idUser, joindre("Contre-proposition envoyée (réponse attendue avant le "
                + ConditionsProposition.date(dateLimite) + ") : " + String.join(" ; ", changements), req.commentaire()));
        communication.propositionEnvoyee(p, true);
        return toDto(p);
    }

    // ------------------------------------------------------------------ retrait

    /** La RH retire une proposition en cours (souvent après un refus) : statut REFUSER, motif obligatoire. */
    public PropositionDto retirer(Integer idProposition, MotifRequest req, SessionUser user) {
        Integer idUser = verifierRh(user);
        PropositionEmbauche p = propositionRepo.findByIdPourModification(idProposition)
                .orElseThrow(() -> PropositionException.introuvable("Proposition introuvable : " + idProposition));
        if (!p.estEnCours()) {
            throw PropositionException.conflit("Seule une proposition en cours peut être retirée (statut : "
                    + p.getStatutActuel() + ")");
        }
        String motif = req == null ? null : blankToNull(req.commentaire());
        if (motif == null) throw PropositionException.invalide("commentaire", "Le motif du retrait est obligatoire");
        histoService.changerStatut(p, PropositionEmbauche.STATUT_REFUSER, idUser, "Proposition retirée par la RH : " + motif);
        propositionRepo.save(p);
        communication.propositionRetiree(p);
        return toDto(p);
    }

    // ------------------------------------------------------------------ contrôles

    /**
     * Date limite de réponse pas encore passée, date de début après la date limite,
     * salaire dans la fourchette de la grille (catégorie et échelon du poste) en vigueur à la date de début.
     */
    void verifierCoherence(BesoinPoste siege, BigDecimal salaire, LocalDate dateDebut, LocalDate dateLimite) {
        LocalDate aujourdhui = LocalDate.now();
        if (dateLimite == null) {
            throw PropositionException.invalide("dateLimiteReponse", "La date limite de réponse est obligatoire");
        }
        if (dateLimite.isBefore(aujourdhui)) {
            throw PropositionException.invalide("dateLimiteReponse", "La date limite de réponse est déjà passée");
        }
        if (dateDebut.isBefore(dateLimite)) {
            throw PropositionException.invalide("dateDebut",
                    "La date de début doit être postérieure ou égale à la date limite de réponse ("
                            + ConditionsProposition.date(dateLimite) + ")");
        }
        Integer idPoste = siege.getPoste().getId();
        FourchetteSalariale f = grilleService.fourchette(idPoste, dateDebut)
                .orElseThrow(() -> new PropositionException(409, "Aucune grille salariale en vigueur au "
                        + ConditionsProposition.date(dateDebut) + " pour le poste " + siege.getPoste().getLibelle(),
                        "salaire"));
        if (!f.contient(salaire)) {
            throw PropositionException.invalide("salaire", "Le salaire doit être compris entre "
                    + ConditionsProposition.montant(f.min()) + " et " + ConditionsProposition.montant(f.max())
                    + " Ar (grille : catégorie " + f.categorie() + ", échelon " + f.echelon() + ")");
        }
    }

    /** null si le siège peut recevoir une proposition, sinon la raison. */
    String raisonIndisponible(BesoinPoste bp) {
        if (ETATS_SIEGE_FERMES.contains(bp.getEtat())) {
            return "Ce poste n'est plus à pourvoir (état : " + bp.getEtat() + ")";
        }
        if (propositionRepo.existsByBesoinPoste_IdAndStatutActuelIn(bp.getId(), STATUTS_EN_COURS)) {
            return "Une proposition est déjà en cours ou acceptée sur ce poste";
        }
        return null;
    }

    private String normaliser(String code, String valeur) {
        return switch (code) {
            case PointCondition.SALAIRE -> ConditionsProposition.stockerSalaire(ConditionsProposition.lireSalaire(valeur));
            case PointCondition.DATE_DEBUT -> ConditionsProposition.lireDate(valeur).toString();
            case PointCondition.TYPE_CONTRAT -> typeContratRepo.findAll().stream()
                    .filter(t -> valeur != null && (t.getLibelle().equalsIgnoreCase(valeur.trim())
                            || String.valueOf(t.getId()).equals(valeur.trim())))
                    .map(t -> String.valueOf(t.getId())).findFirst()
                    .orElseThrow(() -> PropositionException.invalide("idTypeContrat", "Type de contrat inconnu : " + valeur));
            default -> ConditionsProposition.texte(code, valeur, true);
        };
    }

    private Candidature candidatureRetenue(Integer idCandidature) {
        Candidature c = candidatureRepo.findById(idCandidature)
                .orElseThrow(() -> PropositionException.introuvable("Candidature introuvable : " + idCandidature));
        if (!Candidature.ETAT_RETENUE.equals(c.getEtat())) {
            throw PropositionException.conflit("Seule une candidature retenue par la sélection peut recevoir "
                    + "une proposition (état : " + c.getEtat() + ")");
        }
        if (propositionRepo.existsByCandidature_IdAndStatutActuelIn(c.getId(), STATUTS_EN_COURS)) {
            throw PropositionException.conflit("Cette candidature a déjà une proposition en cours ou acceptée");
        }
        return c;
    }

    private OffreEmploi offreDe(Candidature c) {
        return offreRepo.findById(c.getIdOffreEmploi())
                .orElseThrow(() -> PropositionException.introuvable("Offre introuvable : " + c.getIdOffreEmploi()));
    }

    private TypeContrat typeContratParDefaut(OffreEmploi offre) {
        if (offre.getTypeContrat() != null) return offre.getTypeContrat();
        return typeContratRepo.findAll().stream()
                .filter(t -> TYPE_CONTRAT_PAR_DEFAUT.equalsIgnoreCase(t.getLibelle())).findFirst().orElse(null);
    }

    private PointCondition point(String code) {
        return pointRepo.findByLibelle(code).orElseThrow(() -> new IllegalStateException(
                "Point de condition " + code + " absent : exécuter 2026-10-22-18-00-p4-proposition-embauche.sql"));
    }

    private String nouveauJeton() {
        String jeton;
        do {
            byte[] octets = new byte[32];
            RANDOM.nextBytes(octets);
            jeton = Base64.getUrlEncoder().withoutPadding().encodeToString(octets);
        } while (propositionRepo.existsByJeton(jeton));
        return jeton;
    }

    private PropositionEmbauche trouver(Integer id) {
        return propositionRepo.findById(id)
                .orElseThrow(() -> PropositionException.introuvable("Proposition introuvable : " + id));
    }

    public String lienReponse(PropositionEmbauche p) {
        return frontUrl + "/proposition/" + p.getJeton();
    }

    private PropositionDto toDto(PropositionEmbauche p) {
        List<PropositionCondition> conditions = mapper.conditionsTriees(p.getId());
        List<RelanceCondition> relances = mapper.relances(p.getId());
        String etape = PropositionMapper.etape(p.getStatutActuel(), conditions);
        Candidature c = p.getCandidature();
        Candidat ca = c.getCandidat();
        OffreEmploi offre = offreRepo.findById(c.getIdOffreEmploi()).orElse(null);
        BesoinPoste bp = p.getBesoinPoste();

        LocalDate dateDebut = null;
        String dd = valeur(conditions, PointCondition.DATE_DEBUT);
        if (dd != null) dateDebut = ConditionsProposition.lireDate(dd);
        FourchetteSalariale f = grilleService.fourchette(bp.getPoste().getId(), dateDebut).orElse(null);

        Integer idContrat = PropositionEmbauche.STATUT_ACCEPTER.equals(p.getStatutActuel())
                ? propositionRepo.findIdContrat(p.getId()).orElse(null) : null;

        return new PropositionDto(p.getId(), p.getStatutActuel(), etape, p.getDateLimiteReponse(),
                p.getDateEnvoi(), p.getDateReponse(), lienReponse(p),
                c.getId(), ca.getNom(), ca.getPrenom(), ca.getEmail(), ca.getNumero(),
                offre != null ? offre.getId() : null, offre != null ? offre.getRef() : null,
                PropositionMapper.siege(bp, f, false, null),
                f != null ? f.min() : null, f != null ? f.max() : null,
                mapper.conditionsDto(conditions, relances), mapper.relancesDto(relances),
                histoService.historique(p.getId()),
                PropositionMapper.ETAPE_ATTENTE_RH.equals(etape), p.estEnCours(), idContrat);
    }

    static String valeur(List<PropositionCondition> conditions, String code) {
        return conditions.stream().filter(c -> code.equals(c.getCode()))
                .map(PropositionCondition::getValeur).findFirst().orElse(null);
    }

    private static Integer verifierRh(SessionUser user) {
        if (user == null || !user.hasRole(OffreEmploiService.ROLE_RH)) {
            throw new AccesRefuseException("Action réservée au rôle RH");
        }
        return user.getId();
    }

    private static String joindre(String texte, String commentaire) {
        String c = blankToNull(commentaire);
        return c == null ? texte : texte + " — " + c;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
