-- to check what is sequence name for person id, execute query: SELECT * FROM pg_sequences WHERE schemaname = 'public';
SELECT setval('person_id_seq', (SELECT MAX(id) FROM person) + 1);
-- increment size must match 'allocationSize' in EmployeeEntity
ALTER SEQUENCE person_id_seq INCREMENT BY 3;