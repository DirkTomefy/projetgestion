package s5.itu.projetgestion.model.recrutement;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "workflow_candidature")
public class NiveauSelection {

    public static final String TYPE_TEST = "TEST";
    public static final String TYPE_ENTRETIEN_RH = "ENTRETIEN_RH";
    public static final String TYPE_ENTRETIEN_CHEF_DEPARTEMENT = "ENTRETIEN_CHEF_DEPARTEMENT";
    public static final String TYPE_ENTRETIEN_DIRECTION = "ENTRETIEN_DIRECTION";
    public static final List<String> TYPES = List.of(
            TYPE_TEST, TYPE_ENTRETIEN_RH, TYPE_ENTRETIEN_CHEF_DEPARTEMENT, TYPE_ENTRETIEN_DIRECTION);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer ordre;

    @Column(name = "type_evaluation", nullable = false, length = 100)
    private String typeEvaluation;

    /** Note minimale sur 20 pour passer ; NULL = l'évaluateur décide (admis / non admis). */
    @Column(name = "score_min", precision = 5, scale = 2)
    private BigDecimal scoreMin;

    @Column(name = "id_offre_emploi")
    private Integer idOffreEmploi;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
    public String getTypeEvaluation() { return typeEvaluation; }
    public void setTypeEvaluation(String typeEvaluation) { this.typeEvaluation = typeEvaluation; }
    public BigDecimal getScoreMin() { return scoreMin; }
    public void setScoreMin(BigDecimal scoreMin) { this.scoreMin = scoreMin; }
    public Integer getIdOffreEmploi() { return idOffreEmploi; }
    public void setIdOffreEmploi(Integer idOffreEmploi) { this.idOffreEmploi = idOffreEmploi; }
}
