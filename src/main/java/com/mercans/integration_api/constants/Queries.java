package com.mercans.integration_api.constants;

public class Queries {
  // query that inserts in both 'person' and 'salary_component' table applying PG unnest ability
  public static final String UNNEST_INSERT_QUERY =
      """
          INSERT INTO person (id, full_name, employee_code, hire_date, gender, birth_date)
          SELECT * FROM unnest(?::numeric[], ?::text[], ?::text[], ?::date[], ?::text[],?::date[]);

          INSERT INTO salary_component (id, person_id, amount, currency, start_date, end_date)
          SELECT *  FROM unnest(?::numeric[], ?::numeric[], ?::numeric[], ?::text[], ?::date[], ?::date[]);
          """;
}
