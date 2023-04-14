package com.integrador.svfapi.Classes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Student {

    @Id
    @Column(
            name = "student_cod"
    )
    private String studentCod;
    @Column(
            name = "names",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String names;
    @Column(
            name = "last_names",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String lastName;
    @Column(
            name = "birthday",
            nullable = false,
            columnDefinition = "DATE"
    )
    private Date birthday;
    @Column(
            name = "dni",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String dni;
    @Column(
            name = "direction",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String address;
    @Column(
            name = "email",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String email;
    @Column(
            name = "number",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String phone;
    @Column(
            name = "current_grade",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private char currentGrade;
    @Column(
            name = "current_level",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String currentLevel;

}