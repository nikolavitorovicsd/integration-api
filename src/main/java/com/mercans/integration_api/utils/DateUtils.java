package com.mercans.integration_api.utils;

import com.mercans.integration_api.constants.GlobalConstants;
import com.mercans.integration_api.exception.UnskippableCsvException;
import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateUtils {

  // this method tries to return input value in LocalDate format from input "60999"
  // where if day is under 10, it can start without 0
  public LocalDate getLocalDateFromCsvObject(Object csvValue, String fieldName, boolean skippable) {
    try {
      String value = csvValue.toString();
      StringBuilder dateStringBuilder = new StringBuilder();

      String dayPart;
      String monthPart;
      String yearPart;
      int year;

      if (value.length() == 5 || value.length() == 6) {
        if (value.length() == 5) {
          // Format is dMMYY, where day has no leading zero
          dayPart = "0" + value.charAt(0); // Add leading zero to day
          monthPart = value.substring(1, 3);
          yearPart = value.substring(3, 5);
        } else {
          // Format is ddMMyy
          dayPart = value.substring(0, 2);
          monthPart = value.substring(2, 4);
          yearPart = value.substring(4, 6);
        }
        year = Integer.parseInt(yearPart);
        int currentYear = Year.now().getValue() % 100;

        if (year > currentYear) {
          year += 1900; // Consider it as a year in the 1900s
        } else {
          year += 2000; // Consider it as a year in the 2000s
        }
      } else {
        throw new ValidationException(
            String.format("Invalid date format: '%s'. Expected 5 or 6 digits.", value));
      }
      // creates a day in format ddMMyy
      dateStringBuilder.append(dayPart).append("-").append(monthPart).append("-").append(year);

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(GlobalConstants.GLOBAL_DATE_FORMAT);
      return LocalDate.parse(dateStringBuilder, formatter);
    } catch (DateTimeParseException | NullPointerException | ValidationException e) {
      if (skippable) {
        return null;
      }
      throw new UnskippableCsvException(
          String.format(
              "Csv value '%s' for field '%s' couldn't be parsed to LocalDate",
              csvValue, fieldName));
    }
  }

  public String getEmployeeCodeFromStartDate(LocalDate hireDate) {
    // date comes as "2022-01-12", after replace it looks like "20220112", after substring "220112"
    return hireDate.toString().replace("-", "").substring(2, 8) + generateHexadecimalNumber();
  }

  private String generateHexadecimalNumber() {
    Random rand = new Random();
    // generates random number between 0 and 255
    int randomNumber = rand.nextInt(256);
    String hexString = Integer.toHexString(randomNumber);

    // ensures the string is 2 characters long
    if (hexString.length() == 1) {
      hexString = "0" + hexString;
    }
    return hexString;
  }

  // this method validates input value with regex and then tries to return it in LocalDate format
  // "yyyy-MM-dd" from input that looks like "611207NCLTAGZQ8U-NJFVQ5OWYFG" where first 6 figures
  // represent date in yyMMdd format, i.e "1961-12-07" will be saved to db
  public LocalDate getBirthDateFromCsvObject(Object csvValue, String fieldName, boolean skippable) {

    try {
      String inputString = csvValue.toString();

      // regex pattern to validate input format of birthDate
      String regex = "^(\\d{2}(1[0-2]|0[1-9])(3[01]|[12]\\d|0[1-9])).{22}$";
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(inputString);

      // Check if input matches the pattern
      if (matcher.matches()) {
        // extract birthdate part
        String dateString = inputString.substring(0, 6);

        // extract year, month, and day
        String yearPart = dateString.substring(0, 2); // first 2 digits (year)
        String monthPart = dateString.substring(2, 4); // next 2 digits (month)
        String dayPart = dateString.substring(4, 6); // last 2 digits (day)

        // determine the full year (based on current year)
        int year = Integer.parseInt(yearPart);
        int currentYear = Year.now().getValue() % 100; // Get the last 2 digits of current year

        if (year > currentYear) {
          year += 1900; // year in the 1900s
        } else {
          year += 2000; // year in the 2000s
        }

        // create the formatted string "yyyy-MM-dd"
        String formattedDate = String.format("%04d-%s-%s", year, monthPart, dayPart);

        // parse the string into LocalDate using DateTimeFormatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthDate = LocalDate.parse(formattedDate, formatter);

        if (birthDate.isAfter(LocalDate.now())) {
          throw new ValidationException("Birth date of employee cannot be in the future!");
        }
        return birthDate;

      } else {
        throw new ValidationException("Invalid input format for date");
      }
    } catch (DateTimeParseException | NullPointerException | ValidationException e) {
      if (skippable) {
        return null;
      }
      throw new UnskippableCsvException(
          String.format(
              "Csv value '%s' for field '%s' couldn't be parsed to LocalDate",
              csvValue, fieldName));
    }
  }

  public LocalDate getLocalDateForSalaryFromCsvObject(
      Object csvValue, String fieldName, boolean skippable) {
    try {
      String value = csvValue.toString();
      StringBuilder dateStringBuilder = new StringBuilder();

      String dayPart;
      String monthPart;
      String yearPart;
      int year;

      if (value.length() == 5 || value.length() == 6) {
        if (value.length() == 5) {
          // Format is dMMYY, where day has no leading zero
          dayPart = "0" + value.charAt(0); // Add leading zero to day
          monthPart = value.substring(1, 3);
          yearPart = value.substring(3, 5);
        } else {
          // Format is ddMMyy
          dayPart = value.substring(0, 2);
          monthPart = value.substring(2, 4);
          yearPart = value.substring(4, 6);
        }
        year = Integer.parseInt(yearPart) + 2000;
      } else {
        throw new ValidationException(
            String.format("Invalid date format: '%s'. Expected 5 or 6 digits.", value));
      }
      // creates a day in format ddMMyy
      dateStringBuilder.append(dayPart).append("-").append(monthPart).append("-").append(year);

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(GlobalConstants.GLOBAL_DATE_FORMAT);
      return LocalDate.parse(dateStringBuilder, formatter);
    } catch (DateTimeParseException | NullPointerException | ValidationException e) {
      if (skippable) {
        return null;
      }
      throw new UnskippableCsvException(
          String.format(
              "Csv value '%s' for field '%s' couldn't be parsed to LocalDate",
              csvValue, fieldName));
    }
  }
}
