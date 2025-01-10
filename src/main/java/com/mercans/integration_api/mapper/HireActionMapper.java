package com.mercans.integration_api.mapper;

import static com.mercans.integration_api.model.EmployeeRecord.*;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.mercans.integration_api.exception.UnskippableCsvException;
import com.mercans.integration_api.model.EmployeeRecord;
import com.mercans.integration_api.model.actions.Action;
import com.mercans.integration_api.model.actions.HireAction;
import com.mercans.integration_api.model.enums.Gender;
import com.mercans.integration_api.utils.DateUtils;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.*;
import org.apache.commons.lang3.ObjectUtils;

public class HireActionMapper extends HelperMapper implements ActionMapper {
  @Override
  public Action mapToAction(EmployeeRecord employeeRecord, Validator validator)
      throws UnskippableCsvException {
    if (ObjectUtils.isEmpty(employeeRecord.getEmployeeContractStartDate())
        && ObjectUtils.isEmpty(employeeRecord.getEmployeeCode())) {
      throw new UnskippableCsvException(
          "Missing 'employeeCode' and 'hireDate' for 'CHANGE' action");
    }
    // we will try to convert date and if conversion fails it will skip whole row, otherwise
    // it will set it as null which will also skip the row
    LocalDate hireDate =
        DateUtils.getLocalDateFromCsvObject(
            employeeRecord.getEmployeeContractStartDate(), CONTRACT_WORK_START_DATE);

    String employeeCode =
        Optional.ofNullable((String) employeeRecord.getEmployeeCode())
            .orElseGet(() -> DateUtils.getEmployeeCodeFromStartDate(hireDate));

    if (employeeCode.length() > 8) {
      throw new UnskippableCsvException(
          "Invalid 'employeeCode' length, can have at most 8 characters");
    }

    String employeeFullName = getEmployeeFullName(employeeRecord);

    Gender employeeGender = Gender.getGenderFromCsvObject(employeeRecord.getEmployeeGender());

    LocalDate birthDate =
        DateUtils.getBirthDateFromCsvObject(employeeRecord.getEmployeeBirthDate());

    // we validate payComponents and if any have violations we filter them out
    var components =
        buildPayComponents(employeeRecord).stream()
            .filter(component -> isEmpty(validator.validate(component)))
            .toList();

    var hireActionBuilder =
        HireAction.builder()
            .employeeCode(employeeCode)
            .employeeHireDate(hireDate)
            .employeeFullName(employeeFullName)
            .employeGender(employeeGender)
            .employeeBirthDate(birthDate)
            .payComponents(components)
            .build();

    Map<String, Object> data = buildData(hireActionBuilder);

    return hireActionBuilder.toBuilder().data(data).build();
  }

  private Map<String, Object> buildData(HireAction hireAction) {
    Map<String, Object> data = new HashMap<>();
    data.put("person.employee_code", hireAction.employeeCode());
    data.put("person.hire_date", hireAction.employeeHireDate());
    data.put("person.full_name", hireAction.employeeFullName());
    data.put("person.gender", hireAction.employeGender());
    data.put("person.birth_date", hireAction.employeeBirthDate());

    List<Map<String, Object>> componentsList = new ArrayList<>();
    hireAction
        .payComponents()
        .forEach(
            component -> {
              Map<String, Object> componentData = new HashMap<>();
              componentData.put("salary_component.amount", component.amount());
              componentData.put("salary_component.currency", component.currency());
              componentData.put("salary_component.start_date", component.startDate());
              componentData.put("salary_component.end_date", component.endDate());
              componentsList.add(componentData);
            });
    data.put("components", componentsList);
    return data;
  }
}
