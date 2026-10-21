package s5.itu.projetgestion.model.rh;

import jakarta.persistence.*;

@Entity
@Table(name = "document_candidat")
public class DocumentCandidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidature", nullable = false)
    private Candidature candidature;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_document", nullable = false)
    private Document document;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Candidature getCandidature() { return candidature; }
    public void setCandidature(Candidature candidature) { this.candidature = candidature; }
    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }
}
