package s5.itu.projetgestion.repository.embauche;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import s5.itu.projetgestion.model.besoin.BesoinPoste;
import s5.itu.projetgestion.model.embauche.PropositionEmbauche;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PropositionEmbaucheRepository extends JpaRepository<PropositionEmbauche, Integer> {

    Optional<PropositionEmbauche> findByJeton(String jeton);

    /** Verrou pour la réponse du candidat : deux envois simultanés ne peuvent pas lancer deux embauches. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PropositionEmbauche p WHERE p.jeton = :jeton")
    Optional<PropositionEmbauche> findByJetonPourReponse(@Param("jeton") String jeton);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PropositionEmbauche p WHERE p.id = :id")
    Optional<PropositionEmbauche> findByIdPourModification(@Param("id") Integer id);

    boolean existsByCandidature_IdAndStatutActuelIn(Integer idCandidature, Collection<String> statuts);

    boolean existsByBesoinPoste_IdAndStatutActuelIn(Integer idBesoinPoste, Collection<String> statuts);

    boolean existsByJeton(String jeton);

    List<PropositionEmbauche> findByCandidature_IdOrderByIdDesc(Integer idCandidature);

    /** Contrat créé par l'embauche (P3) à partir de cette proposition. */
    @Query(nativeQuery = true, value = "SELECT ct.id FROM contrat ct WHERE ct.id_proposition = :id ORDER BY ct.id LIMIT 1")
    Optional<Integer> findIdContrat(@Param("id") Integer idProposition);

    /** En attente du candidat (au moins une condition sans réponse) et date limite dépassée. */
    @Query("""
            SELECT p FROM PropositionEmbauche p
            WHERE p.statutActuel = 'PROPOSER'
              AND p.dateLimiteReponse < :aujourdhui
              AND EXISTS (SELECT c FROM PropositionCondition c WHERE c.proposition = p AND c.accepte IS NULL)
            """)
    List<PropositionEmbauche> findAExpirer(@Param("aujourdhui") LocalDate aujourdhui);

    /** Sièges d'un besoin (d'un de ses postes si idPoste n'est pas 0), avec le poste, sa catégorie et son échelon. */
    @Query("""
            SELECT bp FROM BesoinPoste bp
            JOIN FETCH bp.poste p
            JOIN FETCH p.metier m
            JOIN FETCH m.categorie
            JOIN FETCH p.echelon
            JOIN FETCH p.uniteOrga
            WHERE bp.besoin.id = :idBesoin AND (:idPoste = 0 OR p.id = :idPoste)
            ORDER BY bp.id
            """)
    List<BesoinPoste> findSiegesDuBesoin(@Param("idBesoin") Integer idBesoin, @Param("idPoste") Integer idPoste);

    @Query(nativeQuery = true, value = """
            SELECT pe.id AS "id", pe.id_statut_actuel AS "statut", pe.date_limite_reponse AS "dateLimiteReponse",
                   pe.date_envoi AS "dateEnvoi", pe.date_reponse AS "dateReponse",
                   c.id AS "idCandidature", ca.nom AS "nom", ca.prenom AS "prenom", ca.email AS "email",
                   o.id AS "idOffre", o.ref AS "referenceOffre", p.libelle AS "poste", pe.id_besoin_poste AS "idBesoinPoste",
                   (SELECT pc.valeur FROM proposition_condition pc JOIN point_condition pt ON pt.id = pc.id_point_condition
                     WHERE pc.id_proposition = pe.id AND pt.libelle = 'SALAIRE') AS "salaire",
                   (SELECT count(*) FROM proposition_condition pc
                     WHERE pc.id_proposition = pe.id AND pc.accepte IS NULL) AS "nbEnAttente",
                   (SELECT count(*) FROM proposition_condition pc
                     WHERE pc.id_proposition = pe.id AND pc.accepte = FALSE) AS "nbRefusees",
                   (SELECT ct.id FROM contrat ct WHERE ct.id_proposition = pe.id ORDER BY ct.id LIMIT 1) AS "idContrat"
            FROM proposition_embauche pe
            JOIN candidatures c ON c.id = pe.id_candidature
            JOIN candidat ca    ON ca.id = c.id_candidat
            JOIN offre_emploi o ON o.id = c.id_offre_emploi
            JOIN besoin_poste bp ON bp.id = pe.id_besoin_poste
            JOIN poste p        ON p.id = bp.id_poste
            WHERE (cast(:statut AS varchar) IS NULL OR pe.id_statut_actuel = :statut)
            ORDER BY pe.id DESC
            """)
    List<PropositionResumeRow> findResumes(@Param("statut") String statut);

    interface PropositionResumeRow {
        Integer getId();
        String getStatut();
        LocalDate getDateLimiteReponse();
        LocalDateTime getDateEnvoi();
        LocalDateTime getDateReponse();
        Integer getIdCandidature();
        String getNom();
        String getPrenom();
        String getEmail();
        Integer getIdOffre();
        String getReferenceOffre();
        String getPoste();
        Integer getIdBesoinPoste();
        String getSalaire();
        Long getNbEnAttente();
        Long getNbRefusees();
        Integer getIdContrat();
    }
}
