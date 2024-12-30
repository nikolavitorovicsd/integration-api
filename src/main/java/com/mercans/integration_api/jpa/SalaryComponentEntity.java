package com.mercans.integration_api.jpa;

import com.mercans.integration_api.model.enums.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "salary_component")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@Getter
@Setter
public class SalaryComponentEntity {

  @Id
//  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "salary_component_id_seq")
//  @SequenceGenerator(
//      name = "salary_component_id_seq",
//      sequenceName = "salary_component_id_seq",
//      allocationSize = 1)
  private BigInteger id;

  @NotNull
  @Column(name = "amount")
  private Long amount;

  @NotNull
  @Column(name = "currency")
  @Enumerated(EnumType.STRING)
  private Currency currency;

  @NotNull
  @Column(name = "start_date")
  private LocalDate startDate;

  @NotNull
  @Column(name = "end_date")
  private LocalDate endDate;

  @Transient private BigInteger employeeId;
}
