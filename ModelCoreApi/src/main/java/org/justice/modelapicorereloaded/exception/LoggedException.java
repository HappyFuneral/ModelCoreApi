package org.justice.modelapicorereloaded.exception;

import org.justice.modelapicorereloaded.ErrorLogger;

public class LoggedException extends Exception {
    private static ErrorLogger errorLogger;

    // Be kell állítani egyszer a pluginból, hogy hol van a plugin mappa (plugin kezdőpontból)
    public static void setErrorLogger(ErrorLogger logger) {
        errorLogger = logger;
    }

    public LoggedException(String message) {
        super(message);
        logSelf();
    }
    public LoggedException(String message, Throwable cause) {
        super(message, cause);
        logSelf();
    }

    private void logSelf() {
        if (errorLogger != null) {
            errorLogger.logError(getClass().getSimpleName(), this);
        } else {
            System.err.println("ErrorLogger nincs beállítva, nem tudok logolni!");
            printStackTrace();
        }
    }
}
