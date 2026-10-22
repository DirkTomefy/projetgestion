package s5.itu.projetgestion.service.personnel;

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
import s5.itu.projetgestion.model.embauche.PropositionCondition;
import s5.itu.projetgestion.model.embauche.PropositionEmbauche;
import s5.itu.projetgestion.model.personnel.Candidat;
import s5.itu.projetgestion.model.postes.Affectation;
import s5.itu.projetgestion.model.postes.Employe;
import s5.itu.projetgestion.model.postes.Poste;
import s5.itu.projetgestion.model.rh.TypeContrat;
import s5.itu.projetgestion.repository.contrat.*;
import s5.itu.projetgestion.repository.besoin.BesoinRepository;
import s5.itu.projetgestion.repository.embauche.PropositionEmbaucheRepository;
import s5.itu.projetgestion.repository.embauche.PropositionConditionRepository;
import s5.itu.projetgestion.repository.rh.TypeContratRepository;
import s5.itu.projetgestion.repository.postes.AffectationRepository;
import s5.itu.projetgestion.repository.postes.BesoinPosteRepository;
import s5.itu.projetgestion.repository.postes.EmployeRepository;

@Service
public class EmployeService {

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

    public EmployeService(EmployeRepository employeRepository, AffectationRepository affectationRepository,
            ContratRepository contratRepository, PropositionConditionRepository propositionConditionRepository,
            ContratDetailRepository contratDetailRepository,
            ContratRemunerationRepository contratRemunerationRepository,
            PropositionEmbaucheRepository propositionEmbaucheRepository,
            BesoinPosteRepository besoinPosteRepository,
            BesoinRepository besoinRepository,
            TypeContratRepository typeContratRepository,
            ContratPdfService contratPdfService) {
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
    }

    @Transactional
    public void embaucher(Integer idProposition) {
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

        Employe employe = creerEmployer(proposition.getCandidature().getCandidat());
        Affectation affectation = creerAffectation(proposition, employe);
        TypeContrat typeContrat = trouverTypeContrat(proposition);
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

    }

    @Transactional
    public Contrat creerContrat(PropositionEmbauche propositionEmbauche, Affectation affectation,
            TypeContrat typeContrat) {
        Contrat contrat = new Contrat();
        contrat.setReference(genererReferenceContrat());
        contrat.setAffectation(affectation);
        contrat.setProposition(propositionEmbauche);
        contrat.setDateDebut(affectation.getDateDebut());
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
        return contrat;
    }

    public Affectation creerAffectation(PropositionEmbauche propositionEmbauche, Employe emp) {
        Affectation affectation = new Affectation();
        affectation.setDateDebut(LocalDate.now());
        affectation.setEmploye(emp);
        affectation.setPoste(propositionEmbauche.getBesoinPoste().getPoste());
        return this.affectationRepository.save(affectation);
    }

    public Employe creerEmployer(Candidat candidat) {
        Employe emp = new Employe();
        emp.fromCandidat(candidat);
        emp.setMatricule(genererMatricule());
        return this.employeRepository.save(emp);
    }

    public String genererMatricule() {
        Long prochain = this.employeRepository.getNextSequenceValue();
        return "EMP000" + prochain;
    }

    private TypeContrat trouverTypeContrat(PropositionEmbauche proposition) {
        Optional<String> valeurId = propositionConditionRepository.findByPropositionId(proposition.getId()).stream()
                .filter(c -> c.getAccepte() == Boolean.TRUE && c.getPointCondition() != null
                        && "id_type_contrat".equalsIgnoreCase(c.getPointCondition().getLibelle()))
                .map(PropositionCondition::getValeur)
                .filter(v -> v != null && !v.isBlank())
                .findFirst();

        if (valeurId.isEmpty()) {
            return typeContratRepository.findByLibelleIgnoreCase("CE")
                    .orElseThrow(() -> new IllegalStateException("Type de contrat CE introuvable"));
        }

        final Integer idTypeContrat;
        try {
            idTypeContrat = Integer.valueOf(valeurId.get().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "L'identifiant du type de contrat doit être numérique : " + valeurId.get(), e);
        }

        return typeContratRepository.findById(idTypeContrat)
                .orElseThrow(() -> new IllegalStateException("Type de contrat introuvable : " + idTypeContrat));
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
