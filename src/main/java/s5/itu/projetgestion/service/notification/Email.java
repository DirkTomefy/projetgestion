package s5.itu.projetgestion.service.notification;

/** Email prêt à partir (contenu HTML déjà produit). */
public record Email(String destinataire, String sujet, String html) {
}
