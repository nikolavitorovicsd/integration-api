# How to run app locally:

1. Run docker-compose.yaml file
2. Write in terminal: ./gradlew bootRun
3. Flyway will pick up all flyway migration scripts and create tables in db (If you modify flyway script after running the application, you should repeat previous steps in order to apply changes otherwise you will get 'Migration checksum mismatch' error )

# How to use the app:

1. Send POST request to following url: "http://localhost:8080/csv/upload"

2. In POST request body include single "*.csv" file after choosing 'form-data' option, and use 'file' for param name (example screenshot attached bellow)

3. Post api will return uuid of generated json response.

4. Reuse that uuid and send GET request to following url to get response
"http://localhost:8080/csv/:uuid"

Post example
![img.png](img.png)

# Important notes:
1. After every job run, there will be a view 'create_employees_current_salaries_view' created in db
2. Application is not thread safe for now and only single batch process should be run
3. CSV file processing takes less than 10s for files up to 12MB