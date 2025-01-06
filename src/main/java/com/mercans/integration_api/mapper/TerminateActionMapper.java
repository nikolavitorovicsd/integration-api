package com.mercans.integration_api.mapper;

import static com.mercans.integration_api.model.EmployeeRecord.CONTRACT_END_DATE;

import com.mercans.integration_api.exception.UnskippableCsvException;
import com.mercans.integration_api.model.EmployeeRecord;
import com.mercans.integration_api.model.actions.Action;
import com.mercans.integration_api.model.actions.TerminateAction;
import com.mercans.integration_api.utils.DateUtils;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Optional;
import org.apache.commons.lang3.ObjectUtils;

public class TerminateActionMapper implements ActionMapper {
  @Override
  public Action mapToAction(EmployeeRecord employeeRecord, Validator validator)
      throws UnskippableCsvException {
    if (ObjectUtils.isEmpty(employeeRecord.getEmployeeCode())) {
      throw new UnskippableCsvException("Missing 'employeeCode' for 'TERMINATE' action");
    }
    LocalDate terminationDate =
        Optional.ofNullable(employeeRecord.getEmployeeContractEndDate())
            // if termination date fails to convert, we throw unskippable exception
            .map(date -> DateUtils.getLocalDateFromCsvObject(date, CONTRACT_END_DATE, false))
            .orElse(LocalDate.now());

    return TerminateAction.builder()
        .employeeCode((String) employeeRecord.getEmployeeCode())
        .terminationDate(terminationDate)
        .build();
  }
}
