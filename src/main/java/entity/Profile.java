package entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Profile entity containing additional user information.
 * One-to-One relationship with User (lazy loaded to avoid unnecessary data fetching).
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One-to-One relationship with User (owning side)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 500)
    private String avatarUrl;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(length = 200)
    private String websiteUrl;

    @Column(length = 100)
    private String linkedinUrl;

    @Column(length = 100)
    private String githubUrl;

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", bio='" + (bio != null ? bio.substring(0, Math.min(bio.length(), 50)) : null) + "...'" +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile)) return false;
        Profile profile = (Profile) o;
        return id != null && id.equals(profile.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
