// package com.mercans.integration_api.model;
//
// import jakarta.validation.Valid;
// import jakarta.validation.ValidationException;
// import jakarta.validation.constraints.NotEmpty;
// import jakarta.validation.constraints.NotNull;
// import java.time.LocalDate;
// import java.time.format.DateTimeFormatter;
// import java.time.format.DateTimeParseException;
// import java.util.List;
// import lombok.Builder;
//
// @Builder(toBuilder = true)
//// @
// public record EmployeeRecordForValidation(
//    @NotEmpty String employeeCode,
//    @NotEmpty Action action,
//    Data data, // todo nullable for now
//    List<@Valid PayComponent> payComponents // todo will have only payment without compensation
//    ) {
//
//  public static EmployeeRecordForValidation of(EmployeeRecord employeeRecord) {
//    var payComponents =
//        List.of(
//            PayComponent.builder()
//                .amount(employeeRecord.payAmount)
//                .currency(Currency.fromClientString(employeeRecord.payCurrency))
//                .startDate(parseDate(employeeRecord.payStartDate))
//                //                .endDate((LocalDate) employeeRecord.payEndDate)
//                .build()
//            //                ,
//            //            PayComponent.builder()
//            //                .amount(employeeRecord.compensationAmount)
//            //                .currency(Currency.valueOf(employeeRecord.compensationCurrency))
//            //                .startDate(LocalDate.parse((CharSequence)
//            // employeeRecord.compensationStartDate))
//            //                .endDate(LocalDate.parse((CharSequence)
//            // employeeRecord.compensationEndDate))
//            //                .build()
//            );
//    return EmployeeRecordForValidation.builder()
//        .employeeCode(employeeRecord.employeeCode)
//        .action(Action.fromClientString(employeeRecord.action))
//        .data(null)
//        .payComponents(payComponents)
//        .build();
//  }
//
//  // this looks like crap
//  private static LocalDate parseDate(@NotNull Object dateInput) {
//
//    try {
//      String value = dateInput.toString();
//
//      StringBuilder formattedDate = new StringBuilder();
//
//      String day;
//      String month;
//      String year;
//
//      if (value.length() == 5) {
//        // Format is dMMYY, where day has no leading zero
//        day = "0" + value.charAt(0); // Add leading zero to day
//        month = value.substring(1, 3);
//        year = "20" + value.substring(3, 5);
//      } else if (value.length() == 6) {
//        // Format is ddMMyy
//        day = value.substring(0, 2);
//        month = value.substring(2, 4);
//        year = "20" + value.substring(4, 6);
//      } else {
//        throw new ValidationException(
//            String.format("Invalid date format: '%s'. Expected 5 or 6 digits.", value));
//      }
//
//      formattedDate.append(day).append("-").append(month).append("-").append(year);
//
//      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//      return LocalDate.parse(formattedDate, formatter);
//    } catch (DateTimeParseException | NullPointerException e) {
//      throw new ValidationException("Invalid date format: " + dateInput, e);
//    }
//  }
// }
