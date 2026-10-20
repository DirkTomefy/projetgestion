package s5.itu.projetgestion.repository.rh;

import org.springframework.data.jpa.repository.JpaRepository;
import s5.itu.projetgestion.model.rh.Document;

public interface DocumentRepository extends JpaRepository<Document, Integer> { }