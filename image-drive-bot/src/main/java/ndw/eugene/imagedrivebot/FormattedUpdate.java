package ndw.eugene.imagedrivebot;

import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public class FormattedUpdate {
    private long chatId;
    private long userId;
    private String messageText;
    private String mediaGroupId;
    private final Message message;
    private Document document;

    //we don't check nullability of message and getFrom() because we consider such updates as invalid and won't process them.
    public FormattedUpdate(Update update) {
        var message = update.getMessage();
        this.message = message;
        this.userId = message.getFrom().getId();
        this.chatId = message.getChatId();
        this.messageText = message.getText();
        this.mediaGroupId = message.getMediaGroupId();
        this.document = message.getDocument();
    }

    public boolean hasDocument() {
        return document != null;
    }

    public boolean messageTextIsNotEmpty() {
        return true; //todo implement condition
    }

    public boolean hasMediaGroup() {
        return mediaGroupId != null;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMediaGroupId() {
        return mediaGroupId;
    }

    public void setMediaGroupId(String mediaGroupId) {
        this.mediaGroupId = mediaGroupId;
    }

    public Message getMessage() {
        return message;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
