package s5.itu.projetgestion.service.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.model.besoin.Besoin;
import s5.itu.projetgestion.model.contrat.Contrat;
import s5.itu.projetgestion.model.recrutement.Candidature;
import s5.itu.projetgestion.model.recrutement.OffreEmploi;
import s5.itu.projetgestion.repository.besoin.BesoinRepository;
import s5.itu.projetgestion.repository.contrat.ContratRepository;
import s5.itu.projetgestion.repository.recrutement.CandidatureRepository;
import s5.itu.projetgestion.repository.recrutement.OffreEmploiRepository;
import s5.itu.projetgestion.service.organisation.EmployeGestionService;

/** Données des exports CSV (première ligne = en-têtes). */
@Service
@Transactional(readOnly = true)
public class ExportService {

    private final EmployeGestionService employeService;
    private final BesoinRepository besoinRepo;
    private final CandidatureRepository candidatureRepo;
    private final OffreEmploiRepository offreRepo;
    private final ContratRepository contratRepo;

    public ExportService(EmployeGestionService employeService, BesoinRepository besoinRepo, CandidatureRepository candidatureRepo,
                         OffreEmploiRepository offreRepo, ContratRepository contratRepo) {
        this.employeService = employeService;
        this.besoinRepo = besoinRepo;
        this.candidatureRepo = candidatureRepo;
        this.offreRepo = offreRepo;
        this.contratRepo = contratRepo;
    }

    public List<List<String>> lignes(String quoi) {
        List<List<String>> l = new ArrayList<>();
        switch (quoi) {
            case "personnel" -> {
                l.add(List.of("Matricule", "Nom", "Prénoms", "Poste", "Unité", "Depuis"));
                employeService.listerTous(null).forEach(e -> l.add(List.of(e.matricule(), e.nom(), s(e.prenoms()), e.poste(), e.unite(), s(e.dateEntree()))));
            }
            case "besoins" -> {
                l.add(List.of("Référence", "Type", "Unité", "Motif", "Effectif", "Date souhaitée", "État", "Coût annuel estimé", "Demandeur"));
                for (Besoin b : besoinRepo.findAllAvecRelations())
                    l.add(List.of(b.getReference(), b.getTypeBesoin(), b.getUniteOrga().getLibelle(), b.getMotif().getLibelle(),
                            s(b.getEffectif()), s(b.getDateSouhaitee()), b.getEtatActuel(),
                            b.getBudgetEstime() == null ? "" : String.format("%.0f", b.getBudgetEstime()), b.getUser().getLogin()));
            }
            case "candidatures" -> {
                Map<Integer, OffreEmploi> offres = offreRepo.findAll().stream().collect(Collectors.toMap(OffreEmploi::getId, Function.identity()));
                l.add(List.of("Offre", "Nom", "Prénom", "Email", "Date de dépôt", "État"));
                for (Candidature c : candidatureRepo.findAll()) {
                    OffreEmploi o = offres.get(c.getIdOffreEmploi());
                    l.add(List.of(o == null ? "" : o.getRef(), c.getCandidat().getNom(), s(c.getCandidat().getPrenom()),
                            s(c.getCandidat().getEmail()), s(c.getDateDepot()), c.getEtat()));
                }
            }
            case "contrats" -> {
                l.add(List.of("Référence", "Matricule", "Employé", "Poste", "Type", "Début", "Fin prévue", "Fin effective", "Statut", "Motif de fin"));
                for (Contrat c : contratRepo.findAll()) {
                    var e = c.getAffectation().getEmploye();
                    l.add(List.of(c.getReference(), e.getMatricule(), e.getNom() + (e.getPrenoms() == null ? "" : " " + e.getPrenoms()),
                            c.getAffectation().getPoste().getLibelle(), c.getType().getLibelle(), s(c.getDateDebut()),
                            s(c.getDateFinPrevue()), s(c.getDateFinEffective()), c.getStatutActuel(), s(c.getMotifFin())));
                }
            }
            default -> throw new IllegalArgumentException("Export inconnu : " + quoi + " (personnel, besoins, candidatures, contrats)");
        }
        return l;
    }

    private static String s(Object o) {
        return o == null ? "" : o.toString();
    }
}
