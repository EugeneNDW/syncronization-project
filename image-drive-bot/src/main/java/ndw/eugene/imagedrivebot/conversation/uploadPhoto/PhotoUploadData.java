package ndw.eugene.imagedrivebot.conversation.uploadPhoto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PhotoUploadData {
    private final List<File> uploadedFiles = new ArrayList<>();
    private String description;

    public void addFile(File file) {
        uploadedFiles.add(file);
    }

    public List<File> getUploadedFiles() {
        return uploadedFiles;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
