DROP VIEW IF EXISTS employees_current_salaries;

CREATE VIEW employees_current_salaries AS
SELECT
    p.id AS person_id,
    p.full_name,
    p.gender,
    p.birth_date,
    p.employee_code,
    p.hire_date,
    sc.amount AS current_salary_amount,
    sc.currency AS salary_currency,
    sc.start_date AS salary_start_date,
    sc.end_date AS salary_end_date
FROM
    person p
INNER JOIN (
    SELECT
        sc1.*
    FROM
        salary_component sc1
    WHERE
        sc1.delete_date IS NULL  -- if salary is still active
        AND (sc1.end_date IS NULL OR sc1.end_date >= CURRENT_DATE)  -- if salary is still valid (not expired)
) sc
ON p.id = sc.person_id
WHERE
    (p.termination_date IS NULL OR p.termination_date > CURRENT_DATE);  -- include active employees only
