package net.bugreaper.modules.db.exceptions;

/**
 * Exceptions that can be thrown during JdbcExecutor use
 */
public class JdbcExecutorException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message
     *
     * @param message String with message
     */
    public JdbcExecutorException(String message) {
        super(message);
    }

}
