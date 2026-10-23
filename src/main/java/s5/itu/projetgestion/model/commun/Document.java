package s5.itu.projetgestion.model.commun;

import jakarta.persistence.*;

@Entity
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_type_document", nullable = false)
    private TypeDocument typeDocument;

    @Column(nullable = false, length = 500)
    private String lien;   // chemin relatif sous la racine d'upload

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public TypeDocument getTypeDocument() { return typeDocument; }
    public void setTypeDocument(TypeDocument typeDocument) { this.typeDocument = typeDocument; }
    public String getLien() { return lien; }
    public void setLien(String lien) { this.lien = lien; }
}