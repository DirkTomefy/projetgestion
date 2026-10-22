package s5.itu.projetgestion.dto.notification;

import java.time.LocalDateTime;

public record NotificationDto(Integer id, String type, String message, String lien, boolean lu,
                              LocalDateTime dateCreation) {
}
