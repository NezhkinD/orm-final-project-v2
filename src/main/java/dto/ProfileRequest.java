package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {

    private String bio;
    private String avatarUrl;
    private String city;
    private String country;
    private String websiteUrl;
    private String linkedinUrl;
    private String githubUrl;
}
