package com.mercans.integration_api.jpa;

import com.mercans.integration_api.model.JsonResponse;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "json_response")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JsonResponseEntity {

  @Id private UUID id;

  @JdbcTypeCode(SqlTypes.JSON)
  private JsonResponse payload;
}
