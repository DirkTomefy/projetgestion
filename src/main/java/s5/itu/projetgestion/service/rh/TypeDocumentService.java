package s5.itu.projetgestion.service.rh;

import org.springframework.stereotype.Service;

import s5.itu.projetgestion.model.rh.TypeDocument;
import s5.itu.projetgestion.repository.rh.TypeDocumentRepository;

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