package com.mercans.integration_api.jpa;

import com.mercans.integration_api.model.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "person")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class EmployeeEntity {

  @Id private Long id;

  @NotNull
  @Column(name = "employee_code", updatable = false)
  private String employeeCode;

  @NotNull
  @Column(name = "hire_date")
  private LocalDate employeeHireDate;

  @NotNull
  @Column(name = "full_name")
  private String employeeFullName;

  @Column(name = "gender")
  @Enumerated(EnumType.STRING)
  private Gender employeGender;

  @Column(name = "birth_date")
  private LocalDate employeeBirthDate;

  @Column(name = "termination_date")
  private LocalDate employeeTerminationDate;

  @OneToMany(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "person_id", nullable = false)
  private List<SalaryComponentEntity> salaryComponents;
}
