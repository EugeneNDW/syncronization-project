package ndw.eugene.imagedrivebot.services;

import ndw.eugene.imagedrivebot.SessionManager;
import ndw.eugene.imagedrivebot.conversation.uploadPhoto.PhotoUploadConversationProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class ConversationService {

    @Autowired
    private final SessionManager sessionManager;

    @Autowired
    private final IFileService fileService;

    @Autowired
    private final TaskScheduler scheduler;

    public ConversationService(SessionManager sessionManager, IFileService fileService, TaskScheduler scheduler) {
        this.sessionManager = sessionManager;
        this.fileService = fileService;
        this.scheduler = scheduler;
    }

    public void startUploadFileConversation(Long userId, Long chatId) {
        var conversationProcessor = new PhotoUploadConversationProcessor(fileService, scheduler);
        sessionManager.startSession(userId, chatId, conversationProcessor);
    }
}
