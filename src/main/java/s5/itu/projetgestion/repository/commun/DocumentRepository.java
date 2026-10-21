package s5.itu.projetgestion.repository.commun;

import org.springframework.data.jpa.repository.JpaRepository;

import s5.itu.projetgestion.model.commun.Document;

public interface DocumentRepository extends JpaRepository<Document, Integer> { }