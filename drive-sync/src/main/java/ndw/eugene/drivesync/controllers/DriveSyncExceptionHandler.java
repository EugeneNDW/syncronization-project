package ndw.eugene.drivesync.controllers;

import ndw.eugene.drivesync.exceptions.DriveException;
import ndw.eugene.drivesync.exceptions.FileNotFoundException;
import ndw.eugene.drivesync.exceptions.FolderNotFoundException;
import ndw.eugene.drivesync.exceptions.TikaException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;

@ControllerAdvice
public class DriveSyncExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {DriveException.class})
    public ResponseEntity<Object> driveExceptionHandler(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_GATEWAY, request);
    }

    @ExceptionHandler(value = {TikaException.class})
    public ResponseEntity<Object> tikaExceptionHandler(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_GATEWAY, request);

    }

    @ExceptionHandler(value = IOException.class)
    public ResponseEntity<Object> ioExceptionHandler(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_GATEWAY, request);
    }

    @ExceptionHandler(value = FolderNotFoundException.class)
    public ResponseEntity<Object> folderNotFoundException(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = FileNotFoundException.class)
    public ResponseEntity<Object> fileNotFoundException(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
}
