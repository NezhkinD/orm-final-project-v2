package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Integer duration;

    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    private Long categoryId;
}
