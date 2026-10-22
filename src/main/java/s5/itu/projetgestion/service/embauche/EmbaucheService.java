package s5.itu.projetgestion.service.embauche;

/**
 * Interface P3 → P4 : embauche automatique quand le candidat a tout accepté.
 *
 * Implémentée par P3 (Tojo) : un @Service qui implémente cette interface. Elle crée l'employé,
 * l'affectation sur proposition_embauche.id_besoin_poste, le contrat à l'état CREE (type accepté,
 * CE par défaut), recopie les conditions acceptées (contrat_details) et le salaire (contrat_remuneration),
 * puis passe le poste à OCCUPE, le siège à POURVU et le besoin à POURVU quand tous ses sièges le sont.
 *
 * Elle s'exécute dans la transaction de l'appelant (PropositionReponseService) : si elle lève
 * une exception, l'acceptation de la proposition est annulée avec elle.
 *
 * Conditions lues (proposition_condition, point_condition.libelle) :
 *   SALAIRE      : montant en Ariary, sans séparateur (ex. "2500000")
 *   DATE_DEBUT   : date ISO (ex. "2026-11-02")
 *   TYPE_CONTRAT : id de type_contrat (ex. "1" ; CE par défaut)
 *   HORAIRE, LIEU, CONDITIONS : texte libre
 */
public interface EmbaucheService {

    /** @return id du contrat créé */
    Integer embaucher(Integer idProposition);
}
