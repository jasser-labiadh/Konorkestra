package dev.jasser.heartBeatReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogManager {
    private static LogManager instance;
    private static final Logger logger = LoggerFactory.getLogger(LogManager.class);
    private LogManager() {}
    public static LogManager getInstance() {
        if (instance == null) {
            synchronized (LogManager.class) {
                if (instance == null) {
                    instance = new LogManager();
                }
            }
        }
        return instance;
    }
    public void logInfo(String message) {
        logger.info(message);
    }

    public void logError(String message) {
        logger.error(message);
    }

    public void logDebug(String message) {
        logger.debug(message);
    }

    public void logWarn(String message) {
        logger.warn(message);
    }
}
