package net.bugreaper.modules.db.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that provides a centralized application logger.
 * <p>
 * This class exposes a shared {@link Logger} instance configured with the name
 * {@code "bugreaper-module-db"}. It is intended to be used across the application to ensure
 * consistent logging configuration and naming.
 *
 * <p>This class cannot be instantiated.
 */
public final class Log {

    /**
     * Shared logger instance for the application.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger("bugreaper-module-db");

    private Log() {
        throw new IllegalStateException("Utility class");
    }
}