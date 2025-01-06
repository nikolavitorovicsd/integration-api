package com.mercans.integration_api.constants;

public class Queries {

  // query that fetches last inserted 'person' PK
  public static final String MAX_PERSON_ID_QUERY =
      """
          SELECT MAX (p.id) FROM person p
          """;

  // query that fetches last inserted 'salary_component' PK
  public static final String MAX_SALARY_COMPONENT_ID_QUERY =
      """
          SELECT MAX (sc.id) FROM salary_component sc
          """;

  // query that inserts in both 'person' and 'salary_component' table applying PG unnest ability
  public static final String UNNEST_INSERT_INTO_PERSON_AND_SALARY_COMPONENT_QUERY =
      """
          INSERT INTO person (id, full_name, employee_code, hire_date, gender, birth_date)
          SELECT * FROM UNNEST(?::NUMERIC[], ?::TEXT[], ?::TEXT[], ?::DATE[], ?::TEXT[],?::DATE[]);

          INSERT INTO salary_component (id, person_id, amount, currency, start_date, end_date)
          SELECT *  FROM UNNEST(?::NUMERIC[], ?::NUMERIC[], ?::NUMERIC[], ?::TEXT[], ?::DATE[], ?::DATE[]);
          """;

  // query that updates 'person' table with fields from ChangeAction applying PG unnest ability
  public static final String UNNEST_UPDATE_PERSON_QUERY =
      """
          UPDATE person
          SET full_name = b.fullName, gender = b.gender, birth_date = b.birthDate, hire_date = b.hireDate
          FROM
          (
              SELECT *
                  FROM
                    UNNEST(?::TEXT[], ?::TEXT[], ?::TEXT[], ?::DATE[], ?::DATE[])
                    AS t(employeeCode, fullName, gender, birthDate, hireDate)
          ) AS b

          WHERE person.employee_code = b.employeeCode

          """;

  // query that inserts into 'salary_component' table applying PG unnest ability
  public static final String UNNEST_INSERT_INTO_SALARY_COMPONENT_QUERY =
      """
          INSERT INTO salary_component (id, person_id, amount, currency, start_date, end_date)
          SELECT * FROM unnest(?::numeric[], ?::numeric[], ?::numeric[], ?::text[], ?::date[], ?::date[]);
          """;

  // query that sets delete_date to 'salary_component' table applying PG unnest ability
  public static final String UNNEST_DELETE_FROM_SALARY_COMPONENT_QUERY =
      """
          UPDATE salary_component AS sc
          SET delete_date = ?
          WHERE (sc.person_id) IN (
            SELECT *
            FROM UNNEST(?::NUMERIC[])
          )
          """;

  // query that terminates contract for 'person'
  public static final String UNNEST_TERMINATE_PERSON_QUERY =
      """
          UPDATE person
          SET termination_date = b.terminationDate
          FROM
          (
              SELECT *
                  FROM
                    UNNEST(?::TEXT[], ?::DATE[])
                    AS t (employeeCode, terminationDate)
          ) AS b

          WHERE person.employee_code = b.employeeCode
          """;
}
