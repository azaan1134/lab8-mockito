# Лаборатори №8 — Mockito mock объектууд

**Оюутан:** С. Азжаргал  
**Оюутны код:** B232270052  
**Хичээл:** F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест  
**Орчин:** Windows 11, PowerShell

## Орчны хувилбар

```
> java -version
openjdk version "17.0.20.1" 2026-08-18
OpenJDK Runtime Environment Temurin-17.0.20.1+1 (build 17.0.20.1+1)
OpenJDK 64-Bit Server VM Temurin-17.0.20.1+1 (build 17.0.20.1+1, mixed mode, sharing)
```

```
> mvn -version
Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Maven home: C:\Program Files\apache-maven-3.9.16
Java version: 17.0.20.1, vendor: Eclipse Adoptium, runtime: C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot
Default locale: en_US, platform encoding: Cp1252
OS name: "windows 11", version: "10.0", arch: "amd64", family: "windows"
```

## Төслийн бүтэц

```
lab8-mockito/
├── pom.xml
├── src/main/java/mn/edu/must/sqat/      
│   ├── Student.java
│   ├── StudentRepository.java
│   ├── EmailSender.java
│   └── CourseRegistrationService.java
├── src/test/java/mn/edu/must/sqat/
│   └── CourseRegistrationServiceTest.java
└── results/
    ├── mvn-test.txt
    ├── mvn-test-mutant-1.txt
    ├── mvn-test-mutant-2.txt
    └── mvn-test-mutant-3.txt
```

## Хэсэг A — Тестүүд

`results/mvn-test.txt`-ийн сүүлийн мөр: **Tests run: 10, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS**
(7 энгийн тест + A8-ийн 3 мөр).

| №  | Тестийн метод | Гол хэрэгсэл |
|----|---------------|--------------|
| A1 | `register_activeStudentWithPrerequisites_returnsOkSavesAndSendsEmail` | `when` + `verify` |
| A2 | `register_unknownStudent_returnsErrorNoStudentAndDoesNotSaveOrEmail` | `verify(..., never())` |
| A3 | `register_inactiveStudent_returnsErrorInactiveAndNeverTouchesEmail` | `verifyNoInteractions` |
| A4 | `register_missingPrerequisite_returnsErrorPrerequisitesAndDoesNotSave` | `when` + `never` |
| A5 | `register_emailSenderThrows_stillReturnsOkAndSaves` | `doThrow(...).when(...)` |
| A6 | `register_nullArgument_throwsIllegalArgumentAndNoInteractions` | `assertThrows` |
| A7 | `register_success_emailBodyContainsCourseId` | `ArgumentCaptor` |
| A8 | `register_errorCases_returnExpectedCodeAndNeverSave` (3 мөр) | `@ParameterizedTest` + `@CsvSource` |

Тестийн нэрс `метод_нөхцөл_хүлээгдэхҮрдүн` хэв маягтай. Matcher хэрэглэсэн дуудалт бүрд бүх аргументад matcher (`eq(...)`, `anyString()`) ашигласан.

### A8 дахь `lenient()` тайлбар

A8-ийн мөр бүр `register()`-ийн өөр цэг дээр зогсдог (жишээ нь `ERROR_NO_STUDENT` мөрд `courseExists`, `prerequisites` огт дуудагдахгүй), тиймээс нийтлэг stub-уудын зарим нь тухайн мөрд ашиглагдахгүй үлдэж, MockitoExtension-ий strict stubs горим `UnnecessaryStubbingException` шиднэ. `lenient().when(...)` нь эдгээр stub-ыг "ашиглагдахгүй байж болно" гэж тэмдэглэж, нэг параметржүүлсэн тестэд гурван өөр замыг шалгах боломж олгодог.

## Хэсэг B — Мини мутаци

| Мутант | Эвдрэл | Унасан тестүүд | Үр дүн (файл) |
|--------|--------|----------------|----------------|
| 1 | `if (!student.active()) return "ERROR_INACTIVE_STUDENT";` комментолсон | A3 `register_inactiveStudent_...` (Failure: `ERROR_NO_COURSE` буцаасан), A8 `[2] exists=true, active=false` (Failure: `OK` буцаасан) | `results/mvn-test-mutant-1.txt` — Tests run: 10, Failures: 2, BUILD FAILURE |
| 2 | `repository.saveRegistration(studentId, courseId);` комментолсон | A1 `register_activeStudentWithPrerequisites_...`, A5 `register_emailSenderThrows_...` (хоёул `Wanted but not invoked: saveRegistration`) | `results/mvn-test-mutant-2.txt` — Tests run: 10, Failures: 2, BUILD FAILURE |
| 3 | `if (studentId == null \|\| courseId == null)` → `if (studentId == null)` | A6 `register_nullArgument_...` (`register("B201", null)` exception шидээгүй) | `results/mvn-test-mutant-3.txt` — Tests run: 10, Failures: 1, BUILD FAILURE |

**Мутант 3-ын тухай:** A6 нь `register(null, courseId)`, `register(studentId, null)`, `register(null, null)` гэсэн гурван тохиолдлыг тус тусад нь шалгадаг тул мутант 3 шууд баригдсан. Хэрэв A6 зөвхөн `register(null, ...)` эсвэл `register(null, null)`-г шалгасан бол `studentId == null` нөхцөл ганцаараа exception шидэх тул мутант амьд үлдэх байсан — тиймээс аргумент тус бүрийн null-ийг тусад нь шалгах хэрэгтэй.

**Дүгнэлт:** Гурван мутант бүгд дор хаяж нэг тестээр баригдсан. Мутант 2-ыг A7 барьдаггүй, учир нь A7 зөвхөн и-мэйлийн агуулгыг шалгадаг — нэг тест нэг зан төлөвийг шалгах нь зөв, харин хадгалалтыг A1, A5 хамгаалж байна.

## Дүгнэлт

Mock нь тест дотор Mockito-гоор үүсгэсэн, хариуг нь `when/thenReturn`-оор тест бүрд тохируулж, дуудалтыг нь `verify`-гоор шалгаж болдог орлуулагч объект юм; харин гараар бичсэн fake нь бодит логиктой (жишээ нь `HashMap` дээр суурилсан repository) хялбаршуулсан хэрэгжүүлэлт бөгөөд төлөвөө өөрөө хадгалж, олон тестэд дахин ашиглагддаг. Mock нь "ямар дуудалт хийгдсэн бэ" гэсэн харилцан үйлчлэлийг шалгахад, fake нь "эцсийн төлөв ямар болсон бэ" гэдгийг шалгахад илүү тохиромжтой. `Student` шиг энгийн өгөгдлийн объект (record), стандарт сангийн `List`, `String` зэргийг mock хийх ёсгүй — бодит объектыг нь шууд үүсгэх нь илүү энгийн бөгөөд найдвартай. Мөн тестлэгдэж буй класс өөрөө (`CourseRegistrationService`) болон өөрийн эзэмшдэггүй гадаад API-г шууд mock хийх нь буруу дадал юм. Verify-г хэтрүүлбэл тест нь бизнесийн үр дүнг биш хэрэгжүүлэлтийн дэлгэрэнгүйг (дуудалтын дараалал, тоо) шалгах болж, код зөв хэвээр рефакторинг хийхэд л тест унадаг эмзэг болно. Иймээс зөвхөн гадагш харагдах үр дагавартай дуудалтыг (хадгалалт, и-мэйл илгээлт) verify хийж, бусад тохиолдолд буцаасан утгыг assert хийх нь зөв. Хэсэг B-ийн мутаци нь тестүүд маань зөвхөн ажиллаад зогсохгүй, кодын алдааг бодитоор илрүүлж чаддагийг харуулсан.
