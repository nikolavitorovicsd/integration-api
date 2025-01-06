package com.mercans.integration_api.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class FileUtils {

  public void deleteFile(String pathToFile) {
    try {
      Path filePath = Paths.get(pathToFile);
      Files.delete(filePath);
    } catch (IOException e) {
      log.error("File for deletion not found, cause: {}", e.getMessage());
      throw new RuntimeException("File you are trying to delete doesn't exist!");
    }
  }

  public void createDirectoryIfMissing(String directoryPath) {
    File directory = new File(directoryPath);
    if (!directory.exists()) {
      directory.mkdirs();
    }
  }

  public String getFileNameWithFormat(String filePath) {
    return filePath.substring(filePath.lastIndexOf("/") + 1);
  }
}
