package ndw.eugene.imagedrivebot.conversations.uploadPhoto;

import org.telegram.telegrambots.meta.api.objects.Document;

import java.util.ArrayList;
import java.util.List;

public class PhotoUploadData {

    private final List<Document> documents = new ArrayList<>();

    private String description;

    public void addFile(Document file) {
        documents.add(file);
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
