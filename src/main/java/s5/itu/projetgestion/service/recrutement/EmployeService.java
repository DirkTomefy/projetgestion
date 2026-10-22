package s5.itu.projetgestion.service.recrutement;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.model.besoin.BesoinPoste;
import s5.itu.projetgestion.model.contrat.Contrat;
import s5.itu.projetgestion.model.contrat.ContratDetail;
import s5.itu.projetgestion.model.contrat.ContratRemuneration;
import s5.itu.projetgestion.model.contrat.EtatContrat;
import s5.itu.projetgestion.model.embauche.PropositionCondition;
import s5.itu.projetgestion.model.embauche.PropositionEmbauche;
import s5.itu.projetgestion.model.recrutement.Candidat;
import s5.itu.projetgestion.model.organisation.Affectation;
import s5.itu.projetgestion.model.organisation.Employe;
import s5.itu.projetgestion.model.organisation.Poste;
import s5.itu.projetgestion.model.contrat.TypeContrat;
import s5.itu.projetgestion.repository.contrat.*;
import s5.itu.projetgestion.repository.besoin.BesoinRepository;
import s5.itu.projetgestion.repository.embauche.PropositionEmbaucheRepository;
import s5.itu.projetgestion.repository.embauche.PropositionConditionRepository;
import s5.itu.projetgestion.repository.organisation.AffectationRepository;
import s5.itu.projetgestion.repository.besoin.BesoinPosteRepository;
import s5.itu.projetgestion.repository.organisation.EmployeRepository;
import s5.itu.projetgestion.service.embauche.EmbaucheService;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Embauche d'un candidat dont la proposition est acceptée (P3).
 * Appelée automatiquement par PropositionReponseService quand le candidat accepte tout (P4),
 * ou à la main par POST /api/embauches.
 */
@Service
public class EmployeService implements EmbaucheService {

    private EmployeRepository employeRepository;
    private AffectationRepository affectationRepository;
    private ContratRepository contratRepository;
    private PropositionConditionRepository propositionConditionRepository;
    private ContratDetailRepository contratDetailRepository;
    private ContratRemunerationRepository contratRemunerationRepository;
    private PropositionEmbaucheRepository propositionEmbaucheRepository;
    private BesoinPosteRepository besoinPosteRepository;
    private BesoinRepository besoinRepository;
    private TypeContratRepository typeContratRepository;
    private ContratPdfService contratPdfService;
    private EtatContratRepository etatContratRepository;

    public EmployeService(EmployeRepository employeRepository, AffectationRepository affectationRepository,
            ContratRepository contratRepository, PropositionConditionRepository propositionConditionRepository,
            ContratDetailRepository contratDetailRepository,
            ContratRemunerationRepository contratRemunerationRepository,
            PropositionEmbaucheRepository propositionEmbaucheRepository,
            BesoinPosteRepository besoinPosteRepository,
            BesoinRepository besoinRepository,
            TypeContratRepository typeContratRepository,
            ContratPdfService contratPdfService,
        EtatContratRepository etatContratRepository) {
        this.employeRepository = employeRepository;
        this.affectationRepository = affectationRepository;
        this.contratRepository = contratRepository;
        this.propositionConditionRepository = propositionConditionRepository;
        this.contratDetailRepository = contratDetailRepository;
        this.contratRemunerationRepository = contratRemunerationRepository;
        this.propositionEmbaucheRepository = propositionEmbaucheRepository;
        this.besoinPosteRepository = besoinPosteRepository;
        this.besoinRepository = besoinRepository;
        this.typeContratRepository = typeContratRepository;
        this.contratPdfService = contratPdfService;
        this.etatContratRepository = etatContratRepository;
    }

    @Override
    @Transactional
    public Integer embaucher(Integer idProposition) {
        PropositionEmbauche proposition = propositionEmbaucheRepository.findById(idProposition)
                .orElseThrow(
                        () -> new IllegalArgumentException("Proposition d'embauche introuvable : " + idProposition));
        if (!PropositionEmbauche.STATUT_ACCEPTER.equals(proposition.getStatutActuel())) {
            throw new IllegalStateException("La proposition doit être acceptée avant l'embauche.");
        }
        if (proposition.getCandidature() == null || proposition.getCandidature().getCandidat() == null) {
            throw new IllegalStateException("La proposition ne possède pas de candidat.");
        }
        if (proposition.getBesoinPoste() == null || proposition.getBesoinPoste().getPoste() == null) {
            throw new IllegalStateException("La proposition ne possède pas de poste.");
        }

        if (contratRepository.existsByPropositionId(idProposition)) {
            throw new IllegalStateException("Cette proposition a déjà donné lieu à une embauche.");
        }

        Map<String, String> conditions = conditionsAcceptees(proposition);
        LocalDate dateDebut = lireDateDebut(conditions.get("DATE_DEBUT"));
        TypeContrat typeContrat = trouverTypeContrat(conditions.get("TYPE_CONTRAT"));

        Employe employe = creerEmployer(proposition.getCandidature().getCandidat());
        Affectation affectation = creerAffectation(proposition, employe, dateDebut);
        Contrat contrat = creerContrat(proposition, affectation, typeContrat);
        recopierRemuneration(proposition, contrat);
        contrat.setDocument(contratPdfService.generateAndStore(contrat));
        contratRepository.save(contrat);

        Poste poste = proposition.getBesoinPoste().getPoste();
        poste.setStatutOccupation("OCCUPE");
        proposition.getBesoinPoste().setEtat("POURVU");
        proposition.getBesoinPoste().setCandidatureRetenue(proposition.getCandidature());

        List<BesoinPoste> lignes = besoinPosteRepository
                .findByBesoinId(proposition.getBesoinPoste().getBesoin().getId());
        if (!lignes.isEmpty() && lignes.stream().allMatch(l -> "POURVU".equals(l.getEtat()))) {
            proposition.getBesoinPoste().getBesoin().setEtatActuel("POURVU");
            besoinRepository.save(proposition.getBesoinPoste().getBesoin());
        }
        besoinPosteRepository.save(proposition.getBesoinPoste());
        return contrat.getId();
    }

    /** Conditions acceptées par le candidat, par libellé de point (SALAIRE, DATE_DEBUT, TYPE_CONTRAT…). */
    private Map<String, String> conditionsAcceptees(PropositionEmbauche proposition) {
        return propositionConditionRepository.findByPropositionId(proposition.getId()).stream()
                .filter(c -> c.getAccepte() == Boolean.TRUE && c.getPointCondition() != null
                        && c.getValeur() != null && !c.getValeur().isBlank())
                .collect(Collectors.toMap(c -> c.getPointCondition().getLibelle().toUpperCase(Locale.ROOT),
                        c -> c.getValeur().trim(), (a, b) -> a));
    }

    /** Date de prise de poste convenue ; à défaut, aujourd'hui. */
    private static LocalDate lireDateDebut(String valeur) {
        if (valeur == null) return LocalDate.now();
        try {
            return LocalDate.parse(valeur);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Date de début invalide dans la proposition : " + valeur, e);
        }
    }

    @Transactional
    public Contrat creerContrat(PropositionEmbauche propositionEmbauche, Affectation affectation,
            TypeContrat typeContrat) {
        Contrat contrat = new Contrat();
        contrat.setReference(genererReferenceContrat());
        contrat.setAffectation(affectation);
        contrat.setProposition(propositionEmbauche);
        contrat.setDateDebut(affectation.getDateDebut());
        if (typeContrat.getDureeMax() != null) {   // CE, CDD : fin prévue = durée maximale du type
            contrat.setDateFinPrevue(affectation.getDateDebut().plusMonths(typeContrat.getDureeMax()).minusDays(1));
        }
        contrat.setStatutActuel("CREE");
        contrat.setType(typeContrat);
        contrat = this.contratRepository.save(contrat);
        List<PropositionCondition> conditions = this.propositionConditionRepository
                .findByPropositionId(propositionEmbauche.getId());

        for (PropositionCondition cond : conditions) {
            if (cond.getAccepte() != Boolean.TRUE) {
                continue;
            }
            ContratDetail detail = new ContratDetail();
            detail.setContrat(contrat);
            detail.setPointCondition(cond.getPointCondition());
            detail.setValeur(cond.getValeur());
            this.contratDetailRepository.save(detail);
        }
        EtatContrat etatContrat = new EtatContrat();
        etatContrat.setContrat(contrat);
        etatContrat.setStatut("CREE");

        this.etatContratRepository.save(etatContrat);

        return contrat;
    }

    public Affectation creerAffectation(PropositionEmbauche propositionEmbauche, Employe emp, LocalDate dateDebut) {
        Affectation affectation = new Affectation();
        affectation.setDateDebut(dateDebut);
        affectation.setEmploye(emp);
        affectation.setPoste(propositionEmbauche.getBesoinPoste().getPoste());
        return this.affectationRepository.save(affectation);
    }

    public Employe creerEmployer(Candidat candidat) {
        Employe emp = new Employe();
        emp.fromCandidat(candidat);
        emp.setPersonne(candidat.getPersonne());   // même personne : on ne ressaisit pas son identité
        emp.setMatricule(genererMatricule());
        return this.employeRepository.save(emp);
    }

    /** Même format que le personnel existant : EMP-2026-021. Ne consomme pas la séquence des id. */
    public String genererMatricule() {
        int annee = LocalDate.now().getYear();
        long prochain = this.employeRepository.count() + 1;
        String matricule = String.format(Locale.ROOT, "EMP-%d-%03d", annee, prochain);
        while (this.employeRepository.existsByMatricule(matricule)) {
            matricule = String.format(Locale.ROOT, "EMP-%d-%03d", annee, ++prochain);
        }
        return matricule;
    }

    /** Type accepté (libellé CE, CDD ou CDI ; un identifiant numérique est aussi toléré) ; CE par défaut. */
    private TypeContrat trouverTypeContrat(String valeur) {
        String v = valeur == null ? "CE" : valeur;
        if (v.chars().allMatch(Character::isDigit)) {
            return typeContratRepository.findById(Integer.valueOf(v))
                    .orElseThrow(() -> new IllegalArgumentException("Type de contrat introuvable : " + v));
        }
        return typeContratRepository.findByLibelleIgnoreCase(v)
                .orElseThrow(() -> new IllegalArgumentException("Type de contrat inconnu : " + v + " (CE, CDD ou CDI)"));
    }

    private void recopierRemuneration(PropositionEmbauche proposition, Contrat contrat) {
        propositionConditionRepository.findByPropositionId(proposition.getId()).stream()
                .filter(c -> c.getAccepte() == Boolean.TRUE && c.getPointCondition() != null
                        && "SALAIRE".equalsIgnoreCase(c.getPointCondition().getLibelle()))
                .map(PropositionCondition::getValeur)
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .ifPresent(valeur -> {
                    ContratRemuneration remuneration = new ContratRemuneration();
                    remuneration.setContrat(contrat);
                    remuneration.setDateEffet(contrat.getDateDebut());
                    remuneration.setSalaire(parseSalaire(valeur));
                    remuneration.setEchelon(contrat.getAffectation().getPoste().getEchelon());
                    contratRemunerationRepository.save(remuneration);
                });
    }

    private double parseSalaire(String valeur) {
        try {
            return Double.parseDouble(valeur.trim().replace(" ", "").replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Salaire invalide dans la proposition : " + valeur, e);
        }
    }

    private String genererReferenceContrat() {
        long prochain = contratRepository.count() + 1;
        String reference = String.format(Locale.ROOT, "CTR-%d-%04d", LocalDate.now().getYear(), prochain);
        while (contratRepository.findByReference(reference).isPresent()) {
            reference = String.format(Locale.ROOT, "CTR-%d-%04d", LocalDate.now().getYear(), ++prochain);
        }
        return reference;
    }

}
