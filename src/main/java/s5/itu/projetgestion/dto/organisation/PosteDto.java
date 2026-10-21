package s5.itu.projetgestion.dto.organisation;

import s5.itu.projetgestion.model.organisation.Poste;

/**
 * Représentation JSON d'un poste : évite de sérialiser les entités LAZY
 * (proxies Hibernate, boucles poste -> unité -> poste...).
 */
public record PosteDto(
        Integer id,
        String code,
        String libelle,
        LibelleDto metier,
        LibelleDto echelon,
        LibelleDto uniteOrga,
        LibelleDto superieurDirect,
        String etat,
        String statutOccupation) {

    public record LibelleDto(Integer id, String libelle) {
    }

    // À appeler dans une transaction (ou avec les relations déjà chargées)
    public static PosteDto from(Poste p) {
        Poste sup = p.getSuperieurDirect();
        return new PosteDto(
                p.getId(),
                p.getCode(),
                p.getLibelle(),
                new LibelleDto(p.getMetier().getId(), p.getMetier().getLibelle()),
                new LibelleDto(p.getEchelon().getId(), p.getEchelon().getLibelle()),
                new LibelleDto(p.getUniteOrga().getId(), p.getUniteOrga().getLibelle()),
                sup == null ? null : new LibelleDto(sup.getId(), sup.getLibelle()),
                p.getEtat(),
                p.getStatutOccupation());
    }
}