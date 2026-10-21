package s5.itu.projetgestion.repository.commun;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.commun.TypeDocument;

import java.util.Optional;

public interface TypeDocumentRepository extends JpaRepository<TypeDocument, Integer> {
    Optional<TypeDocument> findByLibelle(String libelle);
}