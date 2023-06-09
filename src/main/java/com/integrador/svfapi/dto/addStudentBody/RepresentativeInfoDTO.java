package com.integrador.svfapi.dto.addStudentBody;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
public class RepresentativeInfoDTO {
    @NotNull
    @NotBlank
    private String names;
    @NotNull
    @NotBlank
    private String lastnames;
    @NotNull
    @NotBlank
    private String dni;
    @NotNull
    @NotBlank
    private LocalDate birthdate;
    @NotNull
    @NotBlank
    private String direction;
    @NotNull
    @NotBlank
    private String email;
    @NotNull
    @NotBlank
    private String phone;
    @NotNull
    @NotBlank
    private String occupation;
    @NotNull
    @NotBlank
    private String kinship;
}
