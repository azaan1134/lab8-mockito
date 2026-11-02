package mn.edu.must.sqat;

import java.util.Optional;

public class CourseRegistrationService {

    private final StudentRepository repository;
    private final EmailSender emailSender;

    public CourseRegistrationService(StudentRepository repository, EmailSender emailSender) {
        this.repository = repository;
        this.emailSender = emailSender;
    }

    /**
     * Оюутныг хичээлд бүртгэнэ.
     * <ul>
     *   <li>studentId эсвэл courseId null бол IllegalArgumentException; хамаарлуудтай харьцахгүй.</li>
     *   <li>Оюутан олдохгүй бол "ERROR_NO_STUDENT".</li>
     *   <li>Оюутан идэвхгүй бол "ERROR_INACTIVE_STUDENT".</li>
     *   <li>Өмнөх холбоо хичээл дутуу бол "ERROR_PREREQUISITES".</li>
     *   <li>Бусад тохиолдолд бүртгэлийг хадгалж, хичээлийн кодтой и-мэйл илгээж, "OK" буцаана.
     *       И-мэйл илгээхэд алдаа гарсан ч бүртгэл хүчинтэй — "OK".</li>
     * </ul>
     * Алдааны тохиолдолд хадгалалт ба и-мэйл хийгдэхгүй.
     */
    public String register(String studentId, String courseId) {
        if (studentId == null || courseId == null) {
            throw new IllegalArgumentException("studentId and courseId must not be null");
        }

        Optional<Student> found = repository.findById(studentId);
        if (found.isEmpty()) {
            return "ERROR_NO_STUDENT";
        }
        Student student = found.get();

        if (!student.active()) {
            return "ERROR_INACTIVE_STUDENT";
        }

        if (!student.completedCourses().containsAll(repository.getPrerequisites(courseId))) {
            return "ERROR_PREREQUISITES";
        }

        repository.saveRegistration(studentId, courseId);

        try {
            emailSender.send(student.email(),
                    "Бүртгэл баталгаажлаа",
                    "Та " + courseId + " хичээлд амжилттай бүртгүүллээ.");
        } catch (RuntimeException e) {
            // и-мэйлийн алдаа бүртгэлийг цуцлахгүй
        }
        return "OK";
    }
}
