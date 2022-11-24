package ndw.eugene.drivesync.controllers;

import ndw.eugene.drivesync.dto.FileInfoDto;
import ndw.eugene.drivesync.services.IFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/{chatId}/files")
public class FilesController {

    @Autowired
    private final IFileService fileInfoService;

    public FilesController(IFileService fileInfoService) {
        this.fileInfoService = fileInfoService;
    }

    @PostMapping
    public String uploadFiles(
            @PathVariable("chatId") long chatId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "fileInfo") FileInfoDto fileInfo) throws IOException {
        java.io.File tmpFile = multipartToFile(file, fileInfo.name());
        var uploadedFile = fileInfoService.uploadFile(chatId, tmpFile, fileInfo);
        tmpFile.delete();
        return uploadedFile.getId() + " " + uploadedFile.getName(); //todo подумать над респонсом
    }

    @GetMapping
    public FileSystemResource searchFile(@PathVariable("chatId") long chatId, @RequestParam("query") String query, HttpServletResponse response) {
        var file = fileInfoService.searchFile(chatId, query);
        response.setHeader("Content-Disposition", "filename=\"" + file.getName() + "\"");
        return new FileSystemResource(file);
    }

    private java.io.File multipartToFile(MultipartFile file, String fileName) throws IOException {
        var tmpFile = File.createTempFile("tmp", fileName);
        file.transferTo(tmpFile);
        return tmpFile;
    }
}
