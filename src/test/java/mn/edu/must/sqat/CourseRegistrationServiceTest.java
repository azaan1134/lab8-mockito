package mn.edu.must.sqat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;
    CourseRegistrationService service;

    private static final String STUDENT_ID = "B201";
    private static final String COURSE_ID = "CSA313";
    private static final String EMAIL = "b201@must.edu.mn";

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    private Student activeStudent(List<String> coursesTaken) {
        return new Student(STUDENT_ID, EMAIL, true, coursesTaken);
    }

    // A1: when + verify
    @Test
    void register_activeStudentWithPrerequisites_returnsOkSavesAndSendsEmail() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(activeStudent(List.of("CSA101")));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CSA101"));

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("OK", result);
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(EMAIL), anyString(), anyString());
    }

    // A2: verify(..., never())
    @Test
    void register_unknownStudent_returnsErrorNoStudentAndDoesNotSaveOrEmail() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(null);

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_NO_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A3: verifyNoInteractions
    @Test
    void register_inactiveStudent_returnsErrorInactiveAndNeverTouchesEmail() {
        when(repository.findStudent(STUDENT_ID))
                .thenReturn(new Student(STUDENT_ID, EMAIL, false, List.of()));

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_INACTIVE_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    // A4: when + never
    @Test
    void register_missingPrerequisite_returnsErrorPrerequisitesAndDoesNotSave() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(activeStudent(List.of("CSA101")));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CSA101", "CSA202"));

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("ERROR_PREREQUISITES", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A5: doThrow(...).when(...)
    @Test
    void register_emailSenderThrows_stillReturnsOkAndSaves() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(activeStudent(List.of()));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of());
        doThrow(new RuntimeException("SMTP down"))
                .when(emailSender).send(anyString(), anyString(), anyString());

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals("OK", result);
        verify(repository).saveRegistration(STUDENT_ID, COURSE_ID);
        verify(emailSender).send(eq(EMAIL), anyString(), anyString());
    }

    // A6: assertThrows — аргумент тус бүрийн null-ийг тусад нь шалгана
    @Test
    void register_nullArgument_throwsIllegalArgumentAndNoInteractions() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, COURSE_ID));
        assertThrows(IllegalArgumentException.class, () -> service.register(STUDENT_ID, null));
        assertThrows(IllegalArgumentException.class, () -> service.register(null, null));

        verifyNoInteractions(repository, emailSender);
    }

    // A7: ArgumentCaptor
    @Test
    void register_success_emailBodyContainsCourseId() {
        when(repository.findStudent(STUDENT_ID)).thenReturn(activeStudent(List.of()));
        when(repository.courseExists(COURSE_ID)).thenReturn(true);
        when(repository.prerequisites(COURSE_ID)).thenReturn(List.of());
        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        service.register(STUDENT_ID, COURSE_ID);

        verify(emailSender).send(toCaptor.capture(), anyString(), bodyCaptor.capture());
        assertEquals(EMAIL, toCaptor.getValue());
        assertTrue(bodyCaptor.getValue().contains(COURSE_ID),
                "И-мэйлийн биед хичээлийн код байх ёстой: " + bodyCaptor.getValue());
    }

    // A8: @ParameterizedTest + @CsvSource — гурван алдааны анги
    @ParameterizedTest(name = "[{index}] exists={0}, active={1}, hasPrereq={2} -> {3}")
    @CsvSource({
            "false, true,  true,  ERROR_NO_STUDENT",
            "true,  false, true,  ERROR_INACTIVE_STUDENT",
            "true,  true,  false, ERROR_PREREQUISITES"
    })
    void register_errorCases_returnExpectedCodeAndNeverSave(
            boolean exists, boolean active, boolean hasPrereq, String expected) {
        Student student = exists
                ? new Student(STUDENT_ID, EMAIL, active, hasPrereq ? List.of("CSA101") : List.of())
                : null;
        // Мөр бүр өөр цэг дээр зогсдог тул зарим stub ашиглагдахгүй — lenient шаардлагатай.
        lenient().when(repository.findStudent(STUDENT_ID)).thenReturn(student);
        lenient().when(repository.courseExists(COURSE_ID)).thenReturn(true);
        lenient().when(repository.prerequisites(COURSE_ID)).thenReturn(List.of("CSA101"));

        String result = service.register(STUDENT_ID, COURSE_ID);

        assertEquals(expected, result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }
}
