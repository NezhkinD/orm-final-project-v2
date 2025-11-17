package service;

import entity.*;
import exception.BusinessException;
import exception.ResourceNotFoundException;
import repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService Unit Tests")
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private CourseService courseService;

    private Course testCourse;
    private User testTeacher;
    private Category testCategory;
    private entity.Module testModule;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        testTeacher = User.builder()
                .id(1L)
                .name("Teacher")
                .email("teacher@example.com")
                .role(UserRole.TEACHER)
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Programming")
                .build();

        testCourse = Course.builder()
                .id(1L)
                .title("Java Course")
                .description("Learn Java")
                .duration("8 weeks")
                .teacher(testTeacher)
                .category(testCategory)
                .startDate(LocalDate.now())
                .build();

        testModule = entity.Module.builder()
                .id(1L)
                .title("Module 1")
                .description("First module")
                .orderIndex(1)
                .course(testCourse)
                .build();

        testTag = Tag.builder()
                .id(1L)
                .name("Java")
                .build();
    }

    @Test
    @DisplayName("Should create course successfully with teacher")
    void shouldCreateCourseSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testTeacher));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // When
        Course result = courseService.createCourse(testCourse, 1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Java Course");
        verify(userRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should throw exception when non-teacher tries to create course")
    void shouldThrowExceptionWhenNonTeacherCreates() {
        // Given
        User student = User.builder()
                .id(2L)
                .role(UserRole.STUDENT)
                .build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));

        // When & Then
        assertThatThrownBy(() -> courseService.createCourse(testCourse, 2L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("teachers");

        verify(userRepository).findById(2L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Should throw exception when teacher not found")
    void shouldThrowExceptionWhenTeacherNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> courseService.createCourse(testCourse, 999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");

        verify(userRepository).findById(999L);
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("Should get course by id")
    void shouldGetCourseById() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        // When
        Course result = courseService.getCourseById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(courseRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when course not found")
    void shouldThrowExceptionWhenCourseNotFound() {
        // Given
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> courseService.getCourseById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course");

        verify(courseRepository).findById(999L);
    }

    @Test
    @DisplayName("Should get all courses")
    void shouldGetAllCourses() {
        // Given
        List<Course> courses = Arrays.asList(testCourse);
        when(courseRepository.findAll()).thenReturn(courses);

        // When
        List<Course> result = courseService.getAllCourses();

        // Then
        assertThat(result).hasSize(1);
        verify(courseRepository).findAll();
    }

    @Test
    @DisplayName("Should get courses by category")
    void shouldGetCoursesByCategory() {
        // Given
        List<Course> courses = Arrays.asList(testCourse);
        when(courseRepository.findByCategoryId(1L)).thenReturn(courses);

        // When
        List<Course> result = courseService.getCoursesByCategory(1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory().getId()).isEqualTo(1L);
        verify(courseRepository).findByCategoryId(1L);
    }

    @Test
    @DisplayName("Should get courses by teacher")
    void shouldGetCoursesByTeacher() {
        // Given
        List<Course> courses = Arrays.asList(testCourse);
        when(courseRepository.findByTeacherId(1L)).thenReturn(courses);

        // When
        List<Course> result = courseService.getCoursesByTeacher(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(courseRepository).findByTeacherId(1L);
    }

    @Test
    @DisplayName("Should search courses by title")
    void shouldSearchCoursesByTitle() {
        // Given
        List<Course> courses = Arrays.asList(testCourse);
        when(courseRepository.findByTitleContainingIgnoreCase("Java")).thenReturn(courses);

        // When
        List<Course> result = courseService.searchCoursesByTitle("Java");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("Java");
        verify(courseRepository).findByTitleContainingIgnoreCase("Java");
    }

    @Test
    @DisplayName("Should update course successfully")
    void shouldUpdateCourseSuccessfully() {
        // Given
        Course updatedDetails = Course.builder()
                .title("Updated Java Course")
                .description("Updated description")
                .duration("10 weeks")
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // When
        Course result = courseService.updateCourse(1L, updatedDetails);

        // Then
        assertThat(result).isNotNull();
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should delete course successfully")
    void shouldDeleteCourseSuccessfully() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        doNothing().when(courseRepository).delete(testCourse);

        // When
        courseService.deleteCourse(1L);

        // Then
        verify(courseRepository).findById(1L);
        verify(courseRepository).delete(testCourse);
    }

    @Test
    @DisplayName("Should add module to course")
    void shouldAddModuleToCourse() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(moduleRepository.save(any(entity.Module.class))).thenReturn(testModule);

        // When
        entity.Module result = courseService.addModuleToCourse(1L, testModule);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCourse()).isEqualTo(testCourse);
        verify(courseRepository).findById(1L);
        verify(moduleRepository).save(any(entity.Module.class));
    }

    @Test
    @DisplayName("Should add lesson to module")
    void shouldAddLessonToModule() {
        // Given
        Lesson lesson = Lesson.builder()
                .title("Lesson 1")
                .content("Content")
                .orderIndex(1)
                .module(testModule)
                .build();

        when(moduleRepository.findById(1L)).thenReturn(Optional.of(testModule));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);

        // When
        Lesson result = courseService.addLessonToModule(1L, lesson);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getModule()).isEqualTo(testModule);
        verify(moduleRepository).findById(1L);
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    @DisplayName("Should add tags to course")
    void shouldAddTagsToCourse() {
        // Given
        Set<String> tagNames = Set.of("Java", "Programming");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(tagRepository.findByName(anyString())).thenReturn(Optional.of(testTag));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // When
        Course result = courseService.addTagsToCourse(1L, tagNames);

        // Then
        assertThat(result).isNotNull();
        verify(courseRepository).findById(1L);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should remove tag from course")
    void shouldRemoveTagFromCourse() {
        // Given
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        // When
        courseService.removeTagFromCourse(1L, 1L);

        // Then
        verify(courseRepository).findById(1L);
        verify(tagRepository).findById(1L);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("Should get popular courses")
    void shouldGetPopularCourses() {
        // Given
        List<Course> courses = Arrays.asList(testCourse);
        when(courseRepository.findPopularCourses()).thenReturn(courses);

        // When
        List<Course> result = courseService.getPopularCourses();

        // Then
        assertThat(result).hasSize(1);
        verify(courseRepository).findPopularCourses();
    }

    @Test
    @DisplayName("Should get course average rating")
    void shouldGetCourseAverageRating() {
        // Given
        when(courseRepository.getAverageRating(1L)).thenReturn(4.5);

        // When
        Double result = courseService.getCourseAverageRating(1L);

        // Then
        assertThat(result).isEqualTo(4.5);
        verify(courseRepository).getAverageRating(1L);
    }

    @Test
    @DisplayName("Should count active enrollments")
    void shouldCountActiveEnrollments() {
        // Given
        when(courseRepository.countActiveEnrollments(1L)).thenReturn(25L);

        // When
        long result = courseService.countActiveEnrollments(1L);

        // Then
        assertThat(result).isEqualTo(25L);
        verify(courseRepository).countActiveEnrollments(1L);
    }
}
