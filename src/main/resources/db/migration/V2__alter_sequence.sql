-- to check what is sequence name for person id, execute query: SELECT * FROM pg_sequences WHERE schemaname = 'public';

-- increment size must match 'allocationSize' in 'person' entity
--SELECT setval('person_id_seq', (SELECT MAX(id) FROM person) + 1);
--ALTER SEQUENCE person_id_seq INCREMENT BY 1;


-- increment size must match 'allocationSize' in 'salary_component' entity
--SELECT setval('salary_component_id_seq', (SELECT MAX(id) FROM salary_component) + 1);
--ALTER SEQUENCE salary_component_id_seq INCREMENT BY 1;

-- todo commented for now as not really needed