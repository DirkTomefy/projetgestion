package s5.itu.projetgestion.dto.commun;

public record UploadResult(
        Integer id,
        String typeDocument,
        String lien,
        String url,
        String nomOriginal
) { }