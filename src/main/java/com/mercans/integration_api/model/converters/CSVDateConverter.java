package com.mercans.integration_api.model.converters;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvException;
import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.SneakyThrows;

public class CSVDateConverter extends AbstractBeanField<LocalDate, String> {

  @SneakyThrows
  @Override
  protected LocalDate convert(String value) {
    try {

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

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
      return LocalDate.parse(formattedDate, formatter);
    } catch (DateTimeParseException | NullPointerException | ValidationException e) {
      throw new CsvException(String.format(e.getMessage()));
    }
  }
}
