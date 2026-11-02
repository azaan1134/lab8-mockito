package mn.edu.must.sqat;

import java.util.Optional;
import java.util.Set;

/** Өгөгдлийн сан — хэрэгжүүлэлт ЗОРИУД байхгүй. */
public interface StudentRepository {
    Optional<Student> findById(String studentId);

    Set<String> getPrerequisites(String courseId);

    void saveRegistration(String studentId, String courseId);
}
