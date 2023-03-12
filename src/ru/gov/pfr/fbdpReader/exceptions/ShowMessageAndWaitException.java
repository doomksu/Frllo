package ru.gov.pfr.fbdpReader.exceptions;

public class ShowMessageAndWaitException extends Exception {

    public ShowMessageAndWaitException() {
    }

    public ShowMessageAndWaitException(String message) {
        super(message);
    }

    public ShowMessageAndWaitException(String message, Throwable cause) {
        super(message, cause);
    }

}
