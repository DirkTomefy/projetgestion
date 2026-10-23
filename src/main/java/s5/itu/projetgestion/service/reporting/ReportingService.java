package s5.itu.projetgestion.service.reporting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import s5.itu.projetgestion.dto.reporting.TableauDeBordDto;
import s5.itu.projetgestion.repository.reporting.IndicateurRepository;

/** Reporting RH : indicateurs du tableau de bord. */
@Service
@Transactional(readOnly = true)
public class ReportingService {

    private static final BigDecimal DOUZE = BigDecimal.valueOf(12);

    private final IndicateurRepository ind;

    public ReportingService(IndicateurRepository ind) {
        this.ind = ind;
    }

    public TableauDeBordDto tableauDeBord() {
        int exercice = LocalDate.now().getYear();
        long effectif = ind.effectif();
        long actifs = ind.postesParEtat("ACTIF");
        long vacants = ind.postesVacants();
        Map<String, BigDecimal> c = new LinkedHashMap<>();
        c.put("effectif", BigDecimal.valueOf(effectif));
        c.put("postesActifs", BigDecimal.valueOf(actifs));
        c.put("postesVacants", BigDecimal.valueOf(vacants));
        c.put("tauxOccupation", actifs == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf((actifs - vacants) * 100.0 / actifs).setScale(1, RoundingMode.HALF_UP));
        c.put("postesProposes", BigDecimal.valueOf(ind.postesParEtat("PROPOSE")));
        c.put("offresPubliees", BigDecimal.valueOf(ind.offresPubliees()));
        c.put("candidatures30j", BigDecimal.valueOf(ind.candidaturesRecentes(30)));
        c.put("propositionsEnAttente", BigDecimal.valueOf(ind.propositionsEnAttente()));
        c.put("contratsEcheance30j", BigDecimal.valueOf(ind.contratsAEcheance(30)));
        c.put("onboardingsEnCours", BigDecimal.valueOf(ind.onboardingsEnCours()));
        c.put("tachesOnboardingEnRetard", BigDecimal.valueOf(ind.tachesOnboardingEnRetard()));
        BigDecimal masse = ind.masseSalarialeMensuelle();
        c.put("masseSalarialeMensuelle", masse);
        c.put("masseSalarialeAnnuelle", masse.multiply(DOUZE));
        c.put("budgetAnnuel", ind.budgetTotal(exercice));
        BigDecimal delai = ind.delaiMoyenRecrutementJours();
        c.put("delaiMoyenRecrutementJours", delai == null ? null : delai.setScale(1, RoundingMode.HALF_UP));

        List<TableauDeBordDto.Unite> unites = ind.parUnite(exercice).stream()
                .map(u -> new TableauDeBordDto.Unite(u.getIdUnite(), u.getCode(), u.getUnite(), u.getType(), u.getIdUniteMere(),
                        u.getPostes(), u.getOccupes(), u.getVacants(), u.getEffectif(),
                        u.getMasseMensuelle().multiply(DOUZE), u.getBudget(), u.getBesoinsEnCours()))
                .toList();
        return new TableauDeBordDto(c, serie(ind.besoinsParEtat()), serie(ind.entonnoirRecrutement()),
                serie(ind.candidaturesParEtat()), serie(ind.contratsParType()), serie(ind.effectifParCategorie()),
                serie(ind.embauchesParMois()), unites);
    }

    private static List<TableauDeBordDto.Serie> serie(List<IndicateurRepository.Ligne> l) {
        return l.stream().map(x -> new TableauDeBordDto.Serie(x.getLibelle(), x.getValeur())).toList();
    }
}
