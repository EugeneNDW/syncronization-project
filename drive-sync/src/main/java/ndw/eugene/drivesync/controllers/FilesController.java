package ndw.eugene.drivesync.controllers;

import com.google.api.services.drive.model.File;
import ndw.eugene.drivesync.dto.FileInfoDto;
import ndw.eugene.drivesync.services.IGoogleDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/{chatId}/files")
public class FilesController {
    @Autowired
    private final IGoogleDriveService driveService;

    public FilesController(IGoogleDriveService driveService) {
        this.driveService = driveService;
    }

    @PostMapping
    public String uploadFiles(
            @PathVariable("chatId") long chatId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "fileInfo") FileInfoDto fileInfo) throws IOException {
        java.io.File tmpFile = multipartToFile(file, fileInfo.name());
        File uploadedFile = driveService.uploadFIle(chatId, tmpFile, fileInfo);
        tmpFile.delete();
        return uploadedFile.getId() + " " + uploadedFile.getName();
    }

    @DeleteMapping("/{fileId}")
    public String deleteFile(@PathVariable("fileId") String fileId) {
        return driveService.deleteFileById(fileId);
    }

    @GetMapping
    public FileSystemResource searchFile(@PathVariable("chatId") long chatId, @RequestParam("query") String query) {
        var file = driveService.searchFile(chatId, query);
        return new FileSystemResource(file);
    }

    private java.io.File multipartToFile(MultipartFile file, String fileName) throws IOException {
        var convFile = new java.io.File(System.getProperty("java.io.tmpdir"), fileName);
        file.transferTo(convFile);
        return convFile;
    }
}
