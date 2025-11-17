package dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Integer timeLimit;

    private Integer passingScore;
}
