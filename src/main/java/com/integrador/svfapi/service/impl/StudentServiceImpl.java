package com.integrador.svfapi.service.impl;

import com.integrador.svfapi.classes.*;
import com.integrador.svfapi.dto.addStudentBody.AddStudentBodyDTO;
import com.integrador.svfapi.dto.getAllStudents.SingleStudentDTO;
import com.integrador.svfapi.dto.getAllStudents.StudentListDTO;
import com.integrador.svfapi.dto.studentInformation.EnrolledStudentDTO;
import com.integrador.svfapi.dto.studentInformation.NotEnrolledStudent;
import com.integrador.svfapi.dto.updateStudentBody.UpdateStudentInfoDTO;
import com.integrador.svfapi.exception.BusinessException;
import com.integrador.svfapi.repository.*;
import com.integrador.svfapi.service.StudentService;
import com.integrador.svfapi.utils.CodeGenerator;
import com.integrador.svfapi.utils.JwtUtil;
import com.integrador.svfapi.utils.PasswordEncryption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StudentServiceImpl implements StudentService {

    private final JwtUtil jwtUtil;
    private final StudentRepository studentRepository;
    private final RepresentativesRepository representativesRepository;
    private final StudentRepresentativesRepository studentRepresentativesRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CodeGenerator codeGenerator;
    private final PasswordEncryption passwordEncryption;

    @Autowired
    public StudentServiceImpl(
            JwtUtil jwtUtil,
            StudentRepository studentRepository,
            RepresentativesRepository representativesRepository,
            StudentRepresentativesRepository studentRepresentativesRepository,
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository,
            CodeGenerator codeGenerator,
            PasswordEncryption passwordEncryption) {
        this.jwtUtil = jwtUtil;
        this.studentRepository = studentRepository;
        this.representativesRepository = representativesRepository;
        this.studentRepresentativesRepository = studentRepresentativesRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.codeGenerator = codeGenerator;
        this.passwordEncryption = passwordEncryption;
    }

    /*
     * FUNCIONES PARA EL CRUD DE ESTUDIANTES
     */

    /**
     * Este método devuelve la información del estudiante a traves del studentCode que se obtiene a tráves del token.
     * Si el estudiante esta matriculado, retornara un objeto EnrolledStudentDTO,
     * de otra manera retornara un objeto StudentDTO.
     *
     * @param token Token de autenticación.
     * @return ResponseEntity con un objeto personalizado para la respuesta de tipo ResponseFormat.
     */
    @Override
    public ResponseEntity<ResponseFormat> studentInformation(String token) {
        String studentCod = jwtUtil.extractUsername(token);
        Student student = studentRepository.getReferenceById(studentCod);
        Optional <Enrollment> result = Optional.ofNullable(enrollmentRepository.findByStudentCod(studentCod));
        if (result.isPresent()) {
            Enrollment foundEnrollment = result.get();
            EnrolledStudentDTO enrolledStudentDTO = new EnrolledStudentDTO(
                    studentCod,
                    student.getNames(),
                    student.getLastNames(),
                    foundEnrollment.getEnrollmentId());

            String msg = "El estudiante ya cuenta con una matricula registrada";
            return ResponseEntity.ok().body(new ResponseFormat(HttpStatus.OK.value(), msg, enrolledStudentDTO));

        } else {
            String[] newLevelAndGrade;
            newLevelAndGrade = calculateNewLevelAndGrade(student.getCurrentGrade(), student.getCurrentLevel());
            student.setCurrentLevel(newLevelAndGrade[1]);
            student.setCurrentGrade(newLevelAndGrade[0].charAt(0));
            NotEnrolledStudent notEnrolledStudent = new NotEnrolledStudent(
                    studentCod,
                    student.getNames(),
                    student.getLastNames(),
                    student.getDni(),
                    student.getCurrentLevel(),
                    student.getCurrentGrade());

            String msg = "El estudiante aún no cuenta con una matricula registrada";
            return ResponseEntity.ok().body(new ResponseFormat(HttpStatus.OK.value(), msg, notEnrolledStudent));

        }
    }

    /**
     * Este método se usa para obtener la información de todos los estudiantes excluyendo
     * ciertos campos con informacion sensible como contraseñas, salt, entre otros.
     *
     * @return ResponseEntity con un objeto personalizado para la respuesta de tipo ResponseFormat.
     */
    @Override
    public ResponseEntity<ResponseFormat> getAllStudents() {

        List<Student> allStudents = studentRepository.findAll();
        List<StudentListDTO> allStudentsDTO = new ArrayList<>();

        for (Student student: allStudents) {
            Optional<Enrollment> result = Optional.ofNullable(enrollmentRepository.findByStudentCod(student.getStudentCod()));
            boolean isEnrolled;
            isEnrolled = result.isPresent();
            boolean isUserActive = userRepository.getReferenceById(student.getUser().getUserId()).isActive();
            if (isUserActive) {
                DateTimeFormatter originalFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
                LocalDate originalDate = LocalDate.parse(student.getBirthday().toString(), originalFormat);
                DateTimeFormatter newFormat = DateTimeFormatter.ofPattern("yy-MM-dd");
                String studentBirthday = originalDate.format(newFormat);

                StudentListDTO studentListDTO = new StudentListDTO(
                        student.getStudentCod(),
                        student.getNames() + " " + student.getLastNames(),
                        studentBirthday,
                        isEnrolled);
                allStudentsDTO.add(studentListDTO);
            }
        }
        String msg = "Se envía la lista de estudiantes registrados en el sistema";
        return ResponseEntity.ok().body(new ResponseFormat(HttpStatus.OK.value(), msg, allStudentsDTO));
    }

    /**
     * Este método se usa para obtener la información de un unico estudiante usando
     * el código del estudiante como parámetro de búsqueda.
     *
     * @param studentCod Código del estudiante
     * @return ResponseEntity con un objeto personalizado para la respuesta de tipo ResponseFormat.
     */
    @Override
    public ResponseEntity<ResponseFormat> getStudentById(String studentCod) {

        //Student student = studentRepository.findByStudentCod(studentCod);
        Optional<Student> student = studentRepository.findById(studentCod);
        if(student.isPresent()){
            SingleStudentDTO singleStudentDTO = new SingleStudentDTO(
                    student.get().getStudentCod(),
                    student.get().getNames(),
                    student.get().getLastNames(),
                    student.get().getBirthday(),
                    student.get().getDni(),
                    student.get().getAddress(),
                    student.get().getEmail(),
                    student.get().getPhone(),
                    student.get().getCurrentLevel(),
                    student.get().getCurrentGrade()
            );
            return ResponseEntity.ok().body(new ResponseFormat(
                    HttpStatus.OK.value(),
                    HttpStatus.OK.getReasonPhrase(),
                    singleStudentDTO
            ));
        } else {
            throw new BusinessException(HttpStatus.NOT_FOUND, "El estudiante no existe");
        }
    }

    /**
     * Este método se usa para agregar un nuevo registro de un estudiante a la base de datos.
     * Para crear este registro se ingresa como parámetros el token y un objeto DTO que lleva
     * tanto la informacion necesaria para el nuevo registro.
     * El output de este método sera el código del estudiante con su nueva contraseña la cual
     * tendrá el formato por default.
     *
     * @param token Token de autenticación.
     * @param addStudentBodyDTO Objeto con la informacion tanto del estudiante como de su padre o apoderado
     * @return ResponseEntity con un objeto personalizado para la respuesta de tipo ResponseFormat.
     */
    @Override
    public ResponseEntity<ResponseFormat> addStudent(String token, AddStudentBodyDTO addStudentBodyDTO) {
        String studentCod = jwtUtil.extractUsername(token);
        if (jwtUtil.validateToken(token, studentCod)) {
            //Creación del nuevo usuario
            User newUser = new User(
                    codeGenerator.generateNextUserId(userRepository.findTopByOrderByUserIdDesc().getUserId()),
                    true,
                    "A10");
            String salt = passwordEncryption.getSaltvalue(30);
            String defaultPassword = codeGenerator.createDefaultPasswordFormat(
                    addStudentBodyDTO.getStudentInfo().getNames(),
                    addStudentBodyDTO.getStudentInfo().getDni());
            String encryptedPassword = passwordEncryption.generateSecurePassword(defaultPassword, salt);
            //Creación del nuevo estudiante
            Student newStudent = new Student(
                    codeGenerator.generateNextStudentCod(studentRepository.findTopByOrderByStudentCodDesc().getStudentCod()),
                    addStudentBodyDTO.getStudentInfo().getNames(),
                    addStudentBodyDTO.getStudentInfo().getLastnames(),
                    addStudentBodyDTO.getStudentInfo().getBirthdate(),
                    encryptedPassword,
                    salt,
                    addStudentBodyDTO.getStudentInfo().getDni(),
                    addStudentBodyDTO.getStudentInfo().getDirection(),
                    addStudentBodyDTO.getStudentInfo().getEmail(),
                    addStudentBodyDTO.getStudentInfo().getPhone(),
                    addStudentBodyDTO.getStudentInfo().getGrade(),
                    addStudentBodyDTO.getStudentInfo().getLevel(),
                    false,
                    newUser
            );
            //Creación del nuevo padre o apoderado
            Representative newRepresentative = new Representative(
                    addStudentBodyDTO.getRepresentativeInfo().getDni(),
                    addStudentBodyDTO.getRepresentativeInfo().getNames(),
                    addStudentBodyDTO.getRepresentativeInfo().getLastnames(),
                    addStudentBodyDTO.getRepresentativeInfo().getBirthdate(),
                    addStudentBodyDTO.getRepresentativeInfo().getDirection(),
                    addStudentBodyDTO.getRepresentativeInfo().getEmail(),
                    addStudentBodyDTO.getRepresentativeInfo().getPhone(),
                    addStudentBodyDTO.getRepresentativeInfo().getOccupation()
            );
            //Creación de la relación entre el estudiante y su apoderado
            StudentRepresentatives newStudentRepresentatives = new StudentRepresentatives(
                    newStudent,
                    newRepresentative,
                    addStudentBodyDTO.getRepresentativeInfo().getKinship(),
                    codeGenerator.generateNextRelationCod(studentRepresentativesRepository.findTopByOrderByRelationCodeDesc().getRelationCode()));

            Map<String, String> data = new HashMap<>();
            data.put("studentCode", newStudent.getStudentCod());
            data.put("defaultPassword", defaultPassword);
            userRepository.save(newUser);
            studentRepository.save(newStudent);
            representativesRepository.save(newRepresentative);
            studentRepresentativesRepository.save(newStudentRepresentatives);
            String msg = "El registro se realizo correctamente";
            return ResponseEntity.ok().body(new ResponseFormat(HttpStatus.OK.value(), msg, data));

        } else {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

    }

    /**
     * La finalidad de este método es de actualizar la información del registro en la base de datos de un estudiante.
     * Para ello se ingresan como parámetros el token, el código del estudiante y un objeto DTO.
     *
     * @param token Token de autenticación.
     * @param studentCod Código del estudiante.
     * @param updateStudentInfo Objeto DTO que lleva la información a actualizar del registro.
     * @return ResponseEntity con un objeto personalizado para la respuesta de tipo ResponseFormat.
     */
    @Override
    public ResponseEntity<ResponseFormat> updateStudent(String token, String studentCod, UpdateStudentInfoDTO updateStudentInfo) {
        String userCod = jwtUtil.extractUsername(token);
        if (jwtUtil.validateToken(token, userCod)) {
            //Validación de la existencia del registro
            Optional<Student> result = Optional.ofNullable(studentRepository.findByStudentCod(studentCod));
            if (result.isPresent()) {
                Student foundStudent = result.get();
                foundStudent.setNames(updateStudentInfo.getNewNames());
                foundStudent.setLastNames(updateStudentInfo.getNewLastNames());
                foundStudent.setBirthday(updateStudentInfo.getNewBirthday());
                foundStudent.setDni(updateStudentInfo.getNewDni());
                foundStudent.setAddress(updateStudentInfo.getNewAddress());
                foundStudent.setEmail(updateStudentInfo.getNewEmail());
                foundStudent.setPhone(updateStudentInfo.getNewPhone());
                foundStudent.setCurrentGrade(updateStudentInfo.getNewGrade());
                foundStudent.setCurrentLevel(updateStudentInfo.getNewLevel());
                studentRepository.save(foundStudent);
                return ResponseEntity.ok().body(new ResponseFormat(HttpStatus.OK.value(), "La informacion se actualizo correctamente", null));
            } else {
                throw new BusinessException(HttpStatus.NOT_FOUND, "El estudiante no existe");
            }

        } else {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    /**
     * La finalidad de este método es la de iniciar un proceso de eliminación de un registro
     * de estudiante de la base de datos.
     * El proceso inciará por cambiar el estado de usuario del estudiante a inactivo y se otorgara
     * un plazo de 30 días para que la eliminación sea efectiva.
     *
     * @param token Token de autenticación.
     * @param studentCod Código del estudiante.
     * @return ResponseEntity con un objeto personalizado para la respuesta de tipo ResponseFormat.
     */
    @Override
    public ResponseEntity<ResponseFormat> deleteStudent(String token, String studentCod) {
        String userCod = jwtUtil.extractUsername(token);
        if (jwtUtil.validateToken(token, userCod)) {
            //Validación del a existencia del registro
            Optional<Student> result = Optional.ofNullable(studentRepository.findByStudentCod(studentCod));
            if (result.isPresent()) {
                Student foundStudent = result.get();
                User user = userRepository.getReferenceById(foundStudent.getUser().getUserId());
                user.setActive(false);
                userRepository.save(user);
                String msg = "El estado del usuario fue cambiado a inactivo y" +
                            " su cuenta sera borrada del registro en 30 días. ";
                return ResponseEntity.ok().body(new ResponseFormat(HttpStatus.OK.value(), msg, null));
            } else {
                throw new BusinessException(HttpStatus.NOT_FOUND, "El estudiante no existe");
            }
        } else {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }


    /*
        Functions for studentInformation
    */
    /**
     * Método interno usado para realizar los calculos del nuevo grado y nivel
     * a los que pasaría un estudiante en su siguente año de matrícula.
     *
     * @param currentGrade Grado actual del estudiante
     * @param currentLevel Nivel actual del estudiante
     * @return ResponseEntity con un objeto personalizado para la respuesta de tipo ResponseFormat.
     */
    protected String[] calculateNewLevelAndGrade(char currentGrade, String currentLevel) {

        String[] newLevelAndGrade = new String[2];

        if (currentGrade == '6') {
            newLevelAndGrade[0] = "1";
            newLevelAndGrade[1] = "Secundaria";
        } else
        if (currentGrade == '5' && currentLevel.equals("Inicial")) {
            newLevelAndGrade[0] = "1";
            newLevelAndGrade[1] = "Primaria";
        } else
        if (currentGrade == '5' && currentLevel.equals("Secundaria")) {
            newLevelAndGrade[0] = "5";
            newLevelAndGrade[1] = "Secundaria";
        } else {
            int newGrade = currentGrade - '0';
            newLevelAndGrade[0] = String.valueOf(newGrade + 1);
            newLevelAndGrade[1] = currentLevel;
        }
        return newLevelAndGrade;
    }


}
