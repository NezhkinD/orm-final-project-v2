package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String content;

    @NotNull(message = "Order number is required")
    private Integer orderNumber;

    private Integer duration;

    private String videoUrl;
}
