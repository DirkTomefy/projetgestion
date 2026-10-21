package s5.itu.projetgestion.service.commun;

import org.springframework.stereotype.Service;

import s5.itu.projetgestion.model.commun.TypeDocument;
import s5.itu.projetgestion.repository.commun.TypeDocumentRepository;

import java.util.List;

@Service
public class TypeDocumentService {

    private final TypeDocumentRepository repo;

    public TypeDocumentService(TypeDocumentRepository repo) {
        this.repo = repo;
    }

    public List<TypeDocument> findAll() {
        return repo.findAll();
    }
}