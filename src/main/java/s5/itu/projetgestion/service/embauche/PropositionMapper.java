package s5.itu.projetgestion.service.embauche;

import org.springframework.stereotype.Component;
import s5.itu.projetgestion.dto.embauche.ConditionDto;
import s5.itu.projetgestion.dto.embauche.RelanceDto;
import s5.itu.projetgestion.dto.embauche.SiegeDto;
import s5.itu.projetgestion.model.besoin.BesoinPoste;
import s5.itu.projetgestion.model.embauche.PropositionCondition;
import s5.itu.projetgestion.model.embauche.PropositionEmbauche;
import s5.itu.projetgestion.model.embauche.RelanceCondition;
import s5.itu.projetgestion.model.organisation.Poste;
import s5.itu.projetgestion.repository.embauche.PropositionConditionRepository;
import s5.itu.projetgestion.repository.embauche.RelanceConditionRepository;
import s5.itu.projetgestion.service.finance.FourchetteSalariale;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Conversions communes aux pages RH et à la page publique. À appeler dans une transaction. */
@Component
public class PropositionMapper {

    public static final String ETAPE_ATTENTE_CANDIDAT = "ATTENTE_CANDIDAT";
    public static final String ETAPE_ATTENTE_RH = "ATTENTE_RH";
    public static final String ETAPE_ACCEPTEE = "ACCEPTEE";
    public static final String ETAPE_REFUSEE = "REFUSEE";
    public static final String ETAPE_EXPIREE = "EXPIREE";

    private final PropositionConditionRepository conditionRepo;
    private final RelanceConditionRepository relanceRepo;

    public PropositionMapper(PropositionConditionRepository conditionRepo, RelanceConditionRepository relanceRepo) {
        this.conditionRepo = conditionRepo;
        this.relanceRepo = relanceRepo;
    }

    public List<PropositionCondition> conditionsTriees(Integer idProposition) {
        return conditionRepo.findByPropositionId(idProposition).stream()
                .sorted(Comparator.comparingInt((PropositionCondition c) -> ConditionsProposition.rang(c.getCode()))
                        .thenComparing(PropositionCondition::getId))
                .toList();
    }

    public static String etape(String statut, List<PropositionCondition> conditions) {
        return switch (statut) {
            case PropositionEmbauche.STATUT_ACCEPTER -> ETAPE_ACCEPTEE;
            case PropositionEmbauche.STATUT_REFUSER -> ETAPE_REFUSEE;
            case PropositionEmbauche.STATUT_EXPIREE -> ETAPE_EXPIREE;
            default -> conditions.stream().anyMatch(PropositionCondition::enAttente)
                    ? ETAPE_ATTENTE_CANDIDAT : ETAPE_ATTENTE_RH;
        };
    }

    public static String etape(String statut, long nbEnAttente) {
        if (PropositionEmbauche.STATUT_PROPOSER.equals(statut)) {
            return nbEnAttente > 0 ? ETAPE_ATTENTE_CANDIDAT : ETAPE_ATTENTE_RH;
        }
        return etape(statut, List.of());
    }

    public List<ConditionDto> conditionsDto(List<PropositionCondition> conditions, List<RelanceCondition> relances) {
        Map<Integer, RelanceCondition> derniere = new HashMap<>();
        for (RelanceCondition r : relances) {
            derniere.put(r.getPropositionCondition().getId(), r);   // relances triées par date : la dernière gagne
        }
        return conditions.stream().map(c -> {
            RelanceCondition r = derniere.get(c.getId());
            return new ConditionDto(c.getId(), c.getCode(), ConditionsProposition.libelle(c.getCode()),
                    c.getValeur(), ConditionsProposition.afficher(c.getCode(), c.getValeur()),
                    c.getAccepte(), c.getCommentaireCandidat(),
                    r != null, r == null ? null : ConditionsProposition.afficher(c.getCode(), r.getAncienneValeur()));
        }).toList();
    }

    public List<RelanceCondition> relances(Integer idProposition) {
        return relanceRepo.findByPropositionCondition_Proposition_IdOrderByDateRelanceAscIdAsc(idProposition);
    }

    public List<RelanceDto> relancesDto(List<RelanceCondition> relances) {
        return relances.stream().map(r -> {
            String code = r.getPropositionCondition().getCode();
            return new RelanceDto(r.getId(), r.getPropositionCondition().getId(), code,
                    ConditionsProposition.libelle(code),
                    ConditionsProposition.afficher(code, r.getAncienneValeur()),
                    ConditionsProposition.afficher(code, r.getNouvelleValeur()),
                    r.getStatut(), r.getDateRelance(), r.getIdUser(), r.getCommentaire());
        }).toList();
    }

    public static SiegeDto siege(BesoinPoste bp, FourchetteSalariale f, boolean disponible, String indisponibilite) {
        Poste p = bp.getPoste();
        return new SiegeDto(bp.getId(), p.getId(), p.getCode(), p.getLibelle(),
                p.getUniteOrga() != null ? p.getUniteOrga().getLibelle() : null,
                p.getMetier() != null && p.getMetier().getCategorie() != null
                        ? p.getMetier().getCategorie().getLibelle() : null,
                p.getEchelon() != null ? p.getEchelon().getLibelle() : null,
                bp.getEtat(),
                f != null ? f.min() : null, f != null ? f.max() : null,
                disponible, indisponibilite);
    }
}
