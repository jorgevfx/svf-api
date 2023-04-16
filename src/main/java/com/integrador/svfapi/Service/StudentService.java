package com.integrador.svfapi.Service;

import com.integrador.svfapi.Classes.StudentLogin;
import com.integrador.svfapi.Exception.BusinessException;
import com.integrador.svfapi.Repository.StudentRepository;
import com.integrador.svfapi.Classes.Student;
import com.integrador.svfapi.Utils.Encryption;
import com.integrador.svfapi.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private JwtUtil jwtUtil;

    private final Encryption encryption = new Encryption();

    public ResponseEntity<Map<String, String>> login (StudentLogin studentLogin) {

        List<Student> allStudents = studentRepository.findAll();
        Student matchStudent = null;

        for (Student student : allStudents) {
            if (studentLogin.getStudentCod().equals(student.getStudentCod())) {
                matchStudent = student;
                break;
            }
        }

        if(matchStudent == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Student not found");
        }

        if (encryption.verifyUserPassword(
                studentLogin.getPassword(),
                matchStudent.getPassword(),
                matchStudent.getSalt())) {
            String key= jwtUtil.generateToken(studentLogin);
            return ResponseEntity.ok().body(Map.of("key", key ));
        } else {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Login failed");
        }

    }

}
