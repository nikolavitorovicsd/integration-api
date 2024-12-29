package com.mercans.integration_api.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class FileUtils {

  public void compressToGzipFile(String inputFilePath, String outputFilePath) throws IOException {
    // Input file (uncompressed JSON file)
    try (FileInputStream fileInputStream = new FileInputStream(inputFilePath);
        FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream)) {

      // Set the buffer size to 1024 bytes (1 KB)
      byte[] buffer = new byte[1024];
      int length;

      // Read from input file and write to GZIP output stream
      while ((length = fileInputStream.read(buffer)) > 0) {
        gzipOutputStream.write(buffer, 0, length);
      }
    }
  }

  public String decompressGzippedFile(File gzippedFile) throws IOException {
    try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(gzippedFile));
        BufferedReader reader = new BufferedReader(new InputStreamReader(gis))) {

      StringBuilder stringBuilder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line).append("\n");
      }
      return stringBuilder.toString();
    }
  }

  public void deleteFile(String pathToFile) {
    try {
      Path filePath = Paths.get(pathToFile);
      Files.delete(filePath);
    } catch (IOException e) {
      // eg todo nikola rethrow custom exception FailedToDeleteException
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
