package com.mercans.integration_api.config;

import com.mercans.integration_api.model.EmployeeRecord;
import com.mercans.integration_api.model.ProcessedEmployeeRecord;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class JsonProcessor implements ItemProcessor<EmployeeRecord, ProcessedEmployeeRecord> {

  private final Validator validator;

  @Override
  public ProcessedEmployeeRecord process(EmployeeRecord employeeRecord) throws Exception {

    ProcessedEmployeeRecord processedEmployeeRecord =
        ProcessedEmployeeRecord.builder()
            // action is transient and required only during processing
            .action(employeeRecord.getAction())
            // this goes to 'person' table

            .employeeName(employeeRecord.getEmployeeName()) // full_name in person table
            .employeeGender(employeeRecord.getEmployeeGender()) // gender in person table
            // birth date missing // birthdate in person table
            // employeecode is set lower in switch case
            .employeeContractStartDate(
                employeeRecord.getEmployeeContractStartDate()) // hire_date in person table
            //            .employeeContractEndDate(employeeRecord.getEmployeeContractEndDate())

            // this goes to 'salary_component' table as one to many 'person' -> 'salary_component'
            .payAmount(employeeRecord.getPayAmount())
            .payCurrency(employeeRecord.getPayCurrency())
            .payStartDate(employeeRecord.getPayStartDate())
            .payEndDate(employeeRecord.getPayEndDate())
            .compensationAmount(employeeRecord.getCompensationAmount())
            .compensationCurrency(employeeRecord.getCompensationCurrency())
            .compensationStartDate(employeeRecord.getCompensationStartDate())
            .compensationEndDate(employeeRecord.getCompensationEndDate())
            .build();

    // todo finish
    //    If the new hire comes without an employee code, we will generate one based on
    //    their first work day (6 digits: yymmdd) and an order number between 0-255 in
    //    hexadecimal format (2 characters) concatenated as a string.

    // if there are validation exceptions we skip that line
    //    Set<ConstraintViolation<ProcessedEmployeeRecord>> violations =
    //        validator.validate(processedEmployeeRecord);
    //
    //    if (isNotEmpty(violations)) {
    //      throw new ValidationException(violations.toString());
    //    }

    //    return processedEmployeeRecord;

    // todo validate input based on action type and return either processedEmployeeRecord or null
    // (skip)
    switch (employeeRecord.getAction()) {
      case HIRE -> {
        // todo
      }
      case CHANGE -> {
        // todo
      }
      case TERMINATE -> {
        // todo
      }
    }
    return processedEmployeeRecord;
  }
}
