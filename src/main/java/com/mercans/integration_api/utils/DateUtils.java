package com.mercans.integration_api.utils;

import com.mercans.integration_api.constants.GlobalConstants;
import com.mercans.integration_api.exception.UnskippableCsvException;
import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateUtils {

  public LocalDate getLocalDateFromCsvObject(Object csvValue, boolean skippable) {
    try {
      // todo can be extracted and reused in util class
      String value = csvValue.toString();
      StringBuilder formattedDate = new StringBuilder();

      String day;
      String month;
      String year;

      if (value.length() == 5) {
        // Format is dMMYY, where day has no leading zero
        day = "0" + value.charAt(0); // Add leading zero to day
        month = value.substring(1, 3);
        year = "20" + value.substring(3, 5);
      } else if (value.length() == 6) {
        // Format is ddMMyy
        day = value.substring(0, 2);
        month = value.substring(2, 4);
        year = "20" + value.substring(4, 6);
      } else {
        throw new ValidationException(
            String.format("Invalid date format: '%s'. Expected 5 or 6 digits.", value));
      }

      formattedDate.append(day).append("-").append(month).append("-").append(year);

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(GlobalConstants.GLOBAL_DATE_FORMAT);
      return LocalDate.parse(formattedDate, formatter);
    } catch (DateTimeParseException | NullPointerException | ValidationException e) {
      if (skippable) {
        return null;
      }
      throw new UnskippableCsvException(
          String.format("Csv date '%s' couldn't be parsed", csvValue));
    }
  }
}
