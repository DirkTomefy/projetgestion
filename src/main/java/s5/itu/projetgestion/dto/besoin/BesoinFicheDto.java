package s5.itu.projetgestion.dto.besoin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Fiche complète d'un besoin : demande, postes, circuit de validation, budget, historique, offres, actions possibles. */
public record BesoinFicheDto(
        Integer id, String reference, String typeBesoin, String etat,
        Integer idUnite, String unite, Integer idMotif, String motif, String precisionMotif,
        LocalDate dateSouhaitee, Integer effectif, String demandeur, LocalDateTime dateDemande,
        List<Ligne> lignes, List<Etape> circuit, Budget budget, List<Evenement> historique, List<Offre> offres,
        Actions actions) {

    /** Un siège : poste (et son état, PROPOSE s'il est créé par le besoin), employé remplacé, candidat retenu. */
    public record Ligne(Integer id, Integer idPoste, String codePoste, String poste, String etatPoste, String metier,
                        String echelon, String etat, String employeRemplace, String candidatRetenu,
                        BigDecimal salaireMin, BigDecimal salaireMax) { }

    /** Une étape du circuit : statut FAIT, EN_COURS, A_VENIR, REFUSE. */
    public record Etape(Integer ordre, String poste, String statut, String decideur, LocalDateTime date,
                        String commentaire, boolean controleBudget) { }

    /** disponible = budget de l'unité − masse salariale annuelle − autres besoins en cours. */
    public record Budget(int exercice, BigDecimal coutAnnuel, BigDecimal budgetUnite, BigDecimal masseSalariale,
                         BigDecimal autresBesoins, BigDecimal disponible, boolean dansLeBudget) { }

    public record Evenement(LocalDateTime date, String action, String etatAvant, String etatApres, String auteur, String commentaire) { }

    /** Offre d'emploi du besoin ; poste NULL pour une offre qui couvre tout le besoin. */
    public record Offre(Integer id, String reference, String etat, LocalDate datePublication, LocalDate dateLimite, String poste) { }

    /** postesSansOffre : postes du besoin ouvert qui ont encore des places à pourvoir et pas d'offre en cours. */
    public record Actions(boolean valider, boolean refuser, boolean annuler, boolean creerOffre, List<Integer> postesSansOffre) { }
}
