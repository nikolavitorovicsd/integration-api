package com.mercans.integration_api.mapper;

import static com.mercans.integration_api.model.EmployeeRecord.*;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.mercans.integration_api.exception.UnskippableCsvException;
import com.mercans.integration_api.model.EmployeeRecord;
import com.mercans.integration_api.model.actions.Action;
import com.mercans.integration_api.model.actions.ChangeAction;
import com.mercans.integration_api.model.enums.Gender;
import com.mercans.integration_api.utils.DateUtils;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;

public class ChangeActionMapper extends PayComponentMapper implements ActionMapper {

  @Override
  public Action mapToAction(EmployeeRecord employeeRecord, Validator validator)
      throws UnskippableCsvException {
    if (ObjectUtils.isEmpty(employeeRecord.getEmployeeCode())) {
      throw new UnskippableCsvException("Missing 'employeeCode' for 'CHANGE' action");
    }
    LocalDate hireDate =
        DateUtils.getLocalDateFromCsvObject(
            employeeRecord.getEmployeeContractStartDate(), CONTRACT_WORK_START_DATE, false);

    String employeeFullName = (String) employeeRecord.getEmployeeName();

    Gender employeeGender =
        Gender.getGenderFromCsvObject(employeeRecord.getEmployeeGender(), WORKER_GENDER, true);

    LocalDate birthDate =
        DateUtils.getBirthDateFromCsvObject(
            employeeRecord.getEmployeeBirthDate(), WORKER_PERSONAL_CODE, true);

    // we validate payComponents and if any have violations we filter them out
    var components =
        buildPayComponents(employeeRecord).stream()
            .filter(component -> isEmpty(validator.validate(component)))
            .collect(Collectors.toList());

    var changeAction =
        ChangeAction.builder()
            .employeeCode((String) employeeRecord.getEmployeeCode())
            .employeeHireDate(hireDate)
            .employeeFullName(employeeFullName)
            .employeGender(employeeGender)
            .employeeBirthDate(birthDate)
            .payComponents(components)
            .build();

    Map<String, Object> data = buildData(changeAction);

    return changeAction.toBuilder().data(data).build();
  }

  private Map<String, Object> buildData(ChangeAction changeAction) {
    Map<String, Object> data = new HashMap<>();
    data.put("person.hire_date", changeAction.employeeHireDate());
    data.put("person.full_name", changeAction.employeeFullName());
    data.put("person.gender", changeAction.employeGender());
    data.put("person.birth_date", changeAction.employeeBirthDate());

    List<Map<String, Object>> componentsList = new ArrayList<>();
    changeAction
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
