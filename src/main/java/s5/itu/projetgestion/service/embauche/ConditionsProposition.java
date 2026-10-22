package s5.itu.projetgestion.service.embauche;

import s5.itu.projetgestion.err.PropositionException;
import s5.itu.projetgestion.model.embauche.PointCondition;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Libellés, affichage et format de stockage des conditions (point_condition.libelle). */
public final class ConditionsProposition {

    /** Ordre d'affichage. */
    public static final List<String> ORDRE = List.of(
            PointCondition.SALAIRE, PointCondition.DATE_DEBUT, PointCondition.TYPE_CONTRAT,
            PointCondition.HORAIRE, PointCondition.LIEU, PointCondition.CONDITIONS);

    private static final Map<String, String> LIBELLES = Map.of(
            PointCondition.SALAIRE, "Salaire mensuel brut",
            PointCondition.DATE_DEBUT, "Date de début",
            PointCondition.HORAIRE, "Horaires",
            PointCondition.LIEU, "Lieu de travail",
            PointCondition.CONDITIONS, "Conditions de travail",
            PointCondition.TYPE_CONTRAT, "Type de contrat");

    private static final DateTimeFormatter FR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ConditionsProposition() { }

    public static String libelle(String code) {
        return LIBELLES.getOrDefault(code, code);
    }

    public static int rang(String code) {
        int i = ORDRE.indexOf(code);
        return i < 0 ? ORDRE.size() : i;
    }

    /** Valeur lisible par le candidat et la RH. */
    public static String afficher(String code, String valeur) {
        if (valeur == null || valeur.isBlank()) return valeur;
        try {
            if (PointCondition.SALAIRE.equals(code)) {
                return montant(new BigDecimal(valeur)) + " Ar / mois";
            }
            if (PointCondition.DATE_DEBUT.equals(code)) {
                return LocalDate.parse(valeur).format(FR_DATE);
            }
        } catch (NumberFormatException | DateTimeParseException e) {
            return valeur;
        }
        return valeur;
    }

    public static String montant(BigDecimal m) {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.FRANCE);
        s.setGroupingSeparator(' ');
        s.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.##", s).format(m);
    }

    public static String date(LocalDate d) {
        return d == null ? null : d.format(FR_DATE);
    }

    /** Salaire stocké sans séparateur ni zéro inutile : "2500000" (lu tel quel par l'embauche P3). */
    public static String stockerSalaire(BigDecimal salaire) {
        if (salaire == null || salaire.signum() <= 0) {
            throw PropositionException.invalide("salaire", "Le salaire doit être un montant positif");
        }
        if (salaire.stripTrailingZeros().scale() > 2) {
            throw PropositionException.invalide("salaire", "Le salaire a au plus deux décimales");
        }
        return salaire.stripTrailingZeros().toPlainString();
    }

    public static BigDecimal lireSalaire(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw PropositionException.invalide("salaire", "Le salaire est obligatoire");
        }
        try {
            return new BigDecimal(valeur.replaceAll("[\\s\\u00a0\\u202f]", "").replace(',', '.'));
        } catch (NumberFormatException e) {
            throw PropositionException.invalide("salaire", "Salaire invalide : " + valeur);
        }
    }

    public static LocalDate lireDate(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw PropositionException.invalide("dateDebut", "La date de début est obligatoire");
        }
        try {
            return LocalDate.parse(valeur.trim());
        } catch (DateTimeParseException e) {
            throw PropositionException.invalide("dateDebut", "Date invalide (format attendu : AAAA-MM-JJ) : " + valeur);
        }
    }

    /** Texte libre obligatoire (HORAIRE, LIEU) ou facultatif (CONDITIONS). */
    public static String texte(String code, String valeur, boolean obligatoire) {
        String v = valeur == null ? "" : valeur.trim();
        if (v.isEmpty() && obligatoire) {
            throw PropositionException.invalide(champ(code), libelle(code) + " : valeur obligatoire");
        }
        return v.isEmpty() ? null : v;
    }

    public static String champ(String code) {
        return switch (code) {
            case PointCondition.SALAIRE -> "salaire";
            case PointCondition.DATE_DEBUT -> "dateDebut";
            case PointCondition.HORAIRE -> "horaire";
            case PointCondition.LIEU -> "lieu";
            case PointCondition.CONDITIONS -> "conditionsTravail";
            case PointCondition.TYPE_CONTRAT -> "idTypeContrat";
            default -> code;
        };
    }
}
