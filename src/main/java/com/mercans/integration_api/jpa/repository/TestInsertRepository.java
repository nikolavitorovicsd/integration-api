package com.mercans.integration_api.jpa.repository;

import com.mercans.integration_api.model.actions.HireAction;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

// todo remove
@Repository
@RequiredArgsConstructor
public class TestInsertRepository {

  private final EntityManager entityManager;

  @Modifying
  @Transactional
  @Query(
      value =
          """
            INSERT INTO person (full_name,employee_code, hire_date) VALUES (?1, ?2, ?3)
            """,
      nativeQuery = true)
  public int insertIntoPerson(HireAction hireAction) {

    String sql =
        """
                INSERT INTO person (full_name,employee_code, hire_date) VALUES (?1, ?2, ?3)
                """;

    return entityManager
        .createNativeQuery(sql)
        .setParameter(1, hireAction.employeeFullName())
        .setParameter(2, hireAction.employeeCode())
        .setParameter(3, hireAction.employeeHireDate())
        .executeUpdate();
  }
}
