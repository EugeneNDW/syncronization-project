package ndw.eugene.drivesync.controllers;

import ndw.eugene.drivesync.dto.RenameFolderDto;
import ndw.eugene.drivesync.services.IFolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequestMapping("/{chatId}/folders")
@RestController
public class FoldersController {

    @Autowired
    private final IFolderService folderService;

    public FoldersController(IFolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping("/name")
    public void renameFolder(@PathVariable("chatId") long chatId, @RequestBody RenameFolderDto renameFolderDto) throws IOException {
        folderService.renameFolder(chatId, renameFolderDto);
    }
}