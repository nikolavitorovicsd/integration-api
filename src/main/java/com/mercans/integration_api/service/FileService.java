package com.mercans.integration_api.service;

import static com.mercans.integration_api.constants.GlobalConstants.CSV_FILES_UPLOAD_DIRECTORY;

import com.mercans.integration_api.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class FileService {

  public Path saveFileToLocalDirectory(MultipartFile file) {
    try {
      // create directory if missing
      FileUtils.createDirectoryIfMissing(CSV_FILES_UPLOAD_DIRECTORY);

      String fileName = file.getOriginalFilename();

      // csv file path
      Path targetLocation = Paths.get(CSV_FILES_UPLOAD_DIRECTORY + fileName);
      // copy the file to the target location
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      return targetLocation;
    } catch (IOException e) {
      log.error("Failed to upload the file:{}", e.getMessage());
      return null;
    }
  }
}
