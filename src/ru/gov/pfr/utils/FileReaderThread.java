package ru.gov.pfr.utils;

import java.util.concurrent.Callable;

public interface FileReaderThread extends Callable<Boolean> {

    public static String DONE_STATUS = "thread done";

    @Override
    public Boolean call();

    /**
     * Сообщения о состоянии выполнения
     *
     * @return
     */
    public String getStateMessages();

    public String getThreadName();

    public boolean isDone();

}
