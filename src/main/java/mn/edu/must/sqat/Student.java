package mn.edu.must.sqat;

import java.util.Set;

/** Оюутны өгөгдөл. */
public record Student(String id, String name, String email, boolean active, Set<String> completedCourses) {
}
