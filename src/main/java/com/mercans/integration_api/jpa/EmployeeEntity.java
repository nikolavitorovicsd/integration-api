package com.mercans.integration_api.jpa;

import com.mercans.integration_api.model.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "person")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class EmployeeEntity {

  // spotless:off
  // following links were used to optimize bulk insert performancec:
  // https://stackoverflow.com/questions/27697810/why-does-hibernate-disable-insert-batching-when-using-an-identity-identifier-gen?noredirect=1&lq=1
  // https://vladmihalcea.com/how-to-batch-insert-and-update-statements-with-hibernate/
  // https://vladmihalcea.com/migrate-hilo-hibernate-pooled/
  // spotless:on

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "person_id_seq")
  @SequenceGenerator(name = "person_id_seq", sequenceName = "person_id_seq", allocationSize = 3)
  private BigInteger id;

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

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumn(name = "person_id", nullable = false)
  private List<SalaryComponentEntity> salaryComponentEntities;
}
