package com.mercans.integration_api.mapper;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.mercans.integration_api.exception.UnskippableCsvException;
import com.mercans.integration_api.model.EmployeeRecord;
import com.mercans.integration_api.model.actions.Action;
import com.mercans.integration_api.model.actions.ChangeAction;
import com.mercans.integration_api.model.enums.Gender;
import com.mercans.integration_api.utils.DateUtils;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;

public class ChangeActionMapper extends PayComponentBuilder implements ActionMapper {

  @Override
  public Action mapToAction(EmployeeRecord employeeRecord, Validator validator)
      throws UnskippableCsvException {
    if (ObjectUtils.isEmpty(employeeRecord.getEmployeeCode())) {
      throw new UnskippableCsvException("Missing 'employeeCode' for 'CHANGE' action");
    }
    // todo validate employeecode length
    String employeeCode =
        Optional.of(employeeRecord.getEmployeeCode()).map(String.class::cast).orElse(null);

    String employeeFullName = (String) employeeRecord.getEmployeeName();

    Gender employeeGender = Gender.getGenderFromCsvObject(employeeRecord.getEmployeeGender(), true);

    LocalDate birthDate =
        DateUtils.getLocalDateFromCsvObject(employeeRecord.getEmployeeContractStartDate(), true);

    // we validate payComponents and if any have violations we filter them out
    var components =
        buildPayComponents(employeeRecord).stream()
            .filter(component -> isEmpty(validator.validate(component)))
            .collect(Collectors.toSet());

    return ChangeAction.builder()
        .employeeCode(employeeCode)
        .employeeFullName(employeeFullName)
        .employeGender(employeeGender)
        .employeeBirthDate(birthDate)
        .payComponents(components)
        .build();
  }
}