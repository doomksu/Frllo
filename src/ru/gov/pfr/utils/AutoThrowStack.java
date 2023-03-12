package ru.gov.pfr.utils;

/**
 *
 * @author kneretin
 */
public class AutoThrowStack {

    private StringBuilder[] messages;

    public AutoThrowStack(int size) {
        messages = new StringBuilder[size];
    }

    public void insertString(String top) {
        for (int i = messages.length - 2; i > -1; i--) {
            StringBuilder switched = messages[i];
            if (switched != null) {
                messages[i + 1] = switched;
            }
        }
        messages[0] = new StringBuilder(top + "\r\n");
    }

    public String getMessages() {
        StringBuilder val = new StringBuilder();
        for (StringBuilder message : messages) {
            if (message != null) {
                val.append(message);
            }
        }
        return val.toString();
    }

}
