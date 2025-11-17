package config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.learningplatform.entity.*;
import org.example.learningplatform.repository.*;
import org.example.learningplatform.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Initializes the database with sample data on application startup.
 * Demonstrates all ORM features: relationships, lazy loading, cascade operations, etc.
 * NOTE: Only runs in "dev" profile. Use SPRING_PROFILES_ACTIVE=dev to enable.
 */
@Component
@Profile("dev")  // Only run in dev profile (not in test or production)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final QuizService quizService;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== Starting Data Initialization ===");

        // Check if data already exists
        if (userService.getAllUsers().size() > 0) {
            log.info("Data already exists, skipping initialization");
            return;
        }

        try {
            // Step 1: Create categories
            log.info("Creating categories...");
            Category programming = createCategory("Programming", "Learn programming languages and frameworks");
            Category dataSci = createCategory("Data Science", "Machine learning and data analysis");
            Category design = createCategory("Design", "UI/UX and graphic design");

            // Step 2: Create tags
            log.info("Creating tags...");
            Tag javaTag = createTag("Java");
            Tag springTag = createTag("Spring Boot");
            Tag beginnerTag = createTag("Beginner");
            Tag advancedTag = createTag("Advanced");
            Tag pythonTag = createTag("Python");
            Tag mlTag = createTag("Machine Learning");

            // Step 3: Create users
            log.info("Creating users...");
            User admin = createUser("admin@example.com", "Admin User", UserRole.ADMIN, "+1234567890");
            User teacher1 = createUser("john.teacher@example.com", "John Smith", UserRole.TEACHER, "+1234567891");
            User teacher2 = createUser("jane.teacher@example.com", "Jane Doe", UserRole.TEACHER, "+1234567892");
            User student1 = createUser("alice.student@example.com", "Alice Johnson", UserRole.STUDENT, "+1234567893");
            User student2 = createUser("bob.student@example.com", "Bob Wilson", UserRole.STUDENT, "+1234567894");
            User student3 = createUser("charlie.student@example.com", "Charlie Brown", UserRole.STUDENT, "+1234567895");

            // Step 4: Create courses with modules and lessons
            log.info("Creating courses...");
            Course javaCourse = createJavaCourse(teacher1, programming);
            courseService.addTagsToCourse(javaCourse.getId(), Set.of("Java", "Spring Boot", "Beginner"));

            Course pythonCourse = createPythonCourse(teacher2, dataSci);
            courseService.addTagsToCourse(pythonCourse.getId(), Set.of("Python", "Machine Learning", "Advanced"));

            // Step 5: Enroll students
            log.info("Enrolling students...");
            Enrollment enrollment1 = enrollmentService.enrollStudent(student1.getId(), javaCourse.getId());
            Enrollment enrollment2 = enrollmentService.enrollStudent(student2.getId(), javaCourse.getId());
            Enrollment enrollment3 = enrollmentService.enrollStudent(student3.getId(), javaCourse.getId());
            Enrollment enrollment4 = enrollmentService.enrollStudent(student1.getId(), pythonCourse.getId());

            // Update progress for some enrollments
            enrollmentService.updateProgress(enrollment1.getId(), 75);
            enrollmentService.updateProgress(enrollment2.getId(), 50);
            enrollmentService.updateProgress(enrollment4.getId(), 100); // Will auto-complete

            // Step 6: Create assignments and submissions
            log.info("Creating assignments and submissions...");
            createAssignmentsAndSubmissions(javaCourse, student1, student2);

            // Step 7: Create quizzes and submissions
            log.info("Creating quizzes and submissions...");
            createQuizzesAndSubmissions(javaCourse, pythonCourse, student1, student2);

            log.info("=== Data Initialization Completed Successfully ===");
            printSummary();

        } catch (Exception e) {
            log.error("Error during data initialization", e);
        }
    }

    private Category createCategory(String name, String description) {
        Category category = Category.builder()
                .name(name)
                .description(description)
                .build();
        return categoryRepository.save(category);
    }

    private Tag createTag(String name) {
        Tag tag = Tag.builder()
                .name(name)
                .build();
        return tagRepository.save(tag);
    }

    private User createUser(String email, String name, UserRole role, String phoneNumber) {
        User user = User.builder()
                .email(email)
                .name(name)
                .role(role)
                .phoneNumber(phoneNumber)
                .build();
        return userService.createUser(user);
    }

    private Course createJavaCourse(User teacher, Category category) {
        // Create course
        Course course = Course.builder()
                .title("Java Programming: From Basics to Advanced")
                .description("Complete Java programming course covering fundamentals to advanced topics")
                .duration("12 weeks")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(12))
                .build();
        Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());

        // Module 1: Java Basics
        org.example.learningplatform.entity.Module module1 = org.example.learningplatform.entity.Module.builder()
                .title("Java Fundamentals")
                .description("Learn the basics of Java programming")
                .orderIndex(1)
                .build();
        org.example.learningplatform.entity.Module savedModule1 = courseService.addModuleToCourse(savedCourse.getId(), module1);

        Lesson lesson1 = Lesson.builder()
                .title("Introduction to Java")
                .content("Learn about Java history, features, and setup")
                .videoUrl("https://example.com/videos/java-intro")
                .orderIndex(1)
                .durationMinutes(45)
                .build();
        courseService.addLessonToModule(savedModule1.getId(), lesson1);

        Lesson lesson2 = Lesson.builder()
                .title("Variables and Data Types")
                .content("Understanding Java variables, primitives, and reference types")
                .videoUrl("https://example.com/videos/java-variables")
                .orderIndex(2)
                .durationMinutes(60)
                .build();
        courseService.addLessonToModule(savedModule1.getId(), lesson2);

        // Module 2: OOP Concepts
        org.example.learningplatform.entity.Module module2 = org.example.learningplatform.entity.Module.builder()
                .title("Object-Oriented Programming")
                .description("Master OOP principles in Java")
                .orderIndex(2)
                .build();
        org.example.learningplatform.entity.Module savedModule2 = courseService.addModuleToCourse(savedCourse.getId(), module2);

        Lesson lesson3 = Lesson.builder()
                .title("Classes and Objects")
                .content("Creating and using classes in Java")
                .videoUrl("https://example.com/videos/java-classes")
                .orderIndex(1)
                .durationMinutes(75)
                .build();
        courseService.addLessonToModule(savedModule2.getId(), lesson3);

        return savedCourse;
    }

    private Course createPythonCourse(User teacher, Category category) {
        Course course = Course.builder()
                .title("Python for Data Science")
                .description("Learn Python and apply it to data science and machine learning")
                .duration("10 weeks")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(10))
                .build();
        Course savedCourse = courseService.createCourse(course, teacher.getId(), category.getId());

        // Module 1
        org.example.learningplatform.entity.Module module1 = org.example.learningplatform.entity.Module.builder()
                .title("Python Basics")
                .description("Introduction to Python programming")
                .orderIndex(1)
                .build();
        org.example.learningplatform.entity.Module savedModule1 = courseService.addModuleToCourse(savedCourse.getId(), module1);

        Lesson lesson1 = Lesson.builder()
                .title("Python Syntax and Basics")
                .content("Learn Python syntax, variables, and basic operations")
                .videoUrl("https://example.com/videos/python-basics")
                .orderIndex(1)
                .durationMinutes(50)
                .build();
        courseService.addLessonToModule(savedModule1.getId(), lesson1);

        // Module 2
        org.example.learningplatform.entity.Module module2 = org.example.learningplatform.entity.Module.builder()
                .title("Data Analysis with Pandas")
                .description("Learn data manipulation with Pandas library")
                .orderIndex(2)
                .build();
        org.example.learningplatform.entity.Module savedModule2 = courseService.addModuleToCourse(savedCourse.getId(), module2);

        Lesson lesson2 = Lesson.builder()
                .title("Introduction to Pandas")
                .content("Working with DataFrames and Series")
                .videoUrl("https://example.com/videos/pandas-intro")
                .orderIndex(1)
                .durationMinutes(90)
                .build();
        courseService.addLessonToModule(savedModule2.getId(), lesson2);

        return savedCourse;
    }

    private void createAssignmentsAndSubmissions(Course course, User student1, User student2) {
        // Get course with full structure
        Course fullCourse = courseService.getCourseWithFullStructure(course.getId());

        if (fullCourse.getModules().isEmpty()) {
            return;
        }

        org.example.learningplatform.entity.Module firstModule = fullCourse.getModules().get(0);
        if (firstModule.getLessons().isEmpty()) {
            return;
        }

        Lesson firstLesson = firstModule.getLessons().get(0);

        // Create assignment
        Assignment assignment1 = Assignment.builder()
                .title("Java Hello World Exercise")
                .description("Write a simple Hello World program in Java and explain each line")
                .maxScore(100)
                .dueDate(LocalDateTime.now().plusDays(7))
                .build();
        Assignment savedAssignment = assignmentService.createAssignment(firstLesson.getId(), assignment1);

        // Student 1 submits and gets graded
        Submission submission1 = assignmentService.submitAssignment(
                savedAssignment.getId(),
                student1.getId(),
                "public class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}\n\nExplanation: This is a basic Java program that prints 'Hello, World!' to the console."
        );
        assignmentService.gradeSubmission(submission1.getId(), 95, "Excellent work! Clear explanation.");

        // Student 2 submits
        Submission submission2 = assignmentService.submitAssignment(
                savedAssignment.getId(),
                student2.getId(),
                "public class HelloWorld {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, World!\");\n    }\n}"
        );
        assignmentService.gradeSubmission(submission2.getId(), 85, "Good work, but missing explanation.");
    }

    private void createQuizzesAndSubmissions(Course javaCourse, Course pythonCourse, User student1, User student2) {
        // Get courses with modules
        Course javaFull = courseService.getCourseWithModules(javaCourse.getId());

        if (javaFull.getModules().isEmpty()) {
            return;
        }

        // Create quiz for Java course
        org.example.learningplatform.entity.Module javaModule = javaFull.getModules().get(0);

        Quiz javaQuiz = Quiz.builder()
                .title("Java Fundamentals Quiz")
                .description("Test your knowledge of Java basics")
                .passingScore(70)
                .build();
        Quiz savedJavaQuiz = quizService.createQuiz(javaModule.getId(), javaQuiz);

        // Add questions
        Question q1 = Question.builder()
                .text("What is the correct way to declare a variable in Java?")
                .type(QuestionType.SINGLE_CHOICE)
                .points(10)
                .build();
        Question savedQ1 = quizService.addQuestionToQuiz(savedJavaQuiz.getId(), q1);

        quizService.addAnswerOption(savedQ1.getId(), AnswerOption.builder()
                .text("int x = 5;")
                .isCorrect(true)
                .build());
        quizService.addAnswerOption(savedQ1.getId(), AnswerOption.builder()
                .text("var x = 5;")
                .isCorrect(false)
                .build());

        Question q2 = Question.builder()
                .text("Which keyword is used to create a class in Java?")
                .type(QuestionType.SINGLE_CHOICE)
                .points(10)
                .build();
        Question savedQ2 = quizService.addQuestionToQuiz(savedJavaQuiz.getId(), q2);

        AnswerOption q2o1 = quizService.addAnswerOption(savedQ2.getId(), AnswerOption.builder()
                .text("class")
                .isCorrect(true)
                .build());
        AnswerOption q2o2 = quizService.addAnswerOption(savedQ2.getId(), AnswerOption.builder()
                .text("object")
                .isCorrect(false)
                .build());

        // Load quiz with full structure for taking
        Quiz fullQuiz = quizService.getQuizWithFullStructure(savedJavaQuiz.getId());

        // Student 1 takes quiz and gets 100%
        Map<Long, List<Long>> student1Answers = Map.of(
                savedQ1.getId(), List.of(fullQuiz.getQuestions().get(0).getCorrectOptions().get(0).getId()),
                savedQ2.getId(), List.of(q2o1.getId())
        );
        quizService.takeQuiz(savedJavaQuiz.getId(), student1.getId(), student1Answers);

        // Student 2 takes quiz and gets 50%
        Map<Long, List<Long>> student2Answers = Map.of(
                savedQ1.getId(), List.of(fullQuiz.getQuestions().get(0).getOptions().get(1).getId()),
                savedQ2.getId(), List.of(q2o1.getId())
        );
        quizService.takeQuiz(savedJavaQuiz.getId(), student2.getId(), student2Answers);
    }

    private void printSummary() {
        log.info("=== Database Summary ===");
        log.info("Total Users: {}", userService.getAllUsers().size());
        log.info("Total Courses: {}", courseService.getAllCourses().size());
        log.info("Total Categories: {}", categoryRepository.count());
        log.info("Total Tags: {}", tagRepository.count());

        log.info("\n=== Sample Queries ===");
        log.info("Students: {}", userService.getUsersByRole(UserRole.STUDENT).size());
        log.info("Teachers: {}", userService.getUsersByRole(UserRole.TEACHER).size());

        List<Course> popularCourses = courseService.getPopularCourses();
        if (!popularCourses.isEmpty()) {
            log.info("Most popular course: {}", popularCourses.get(0).getTitle());
        }

        log.info("========================");
    }
}
