package net.bugreaper.modules.db.exceptions;

/**
 * Exceptions that can be thrown during QueryBuilder use
 */
public class QueryBuilderException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message
     *
     * @param message String with message
     */
    public QueryBuilderException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message String with message
     * @param cause the cause
     */
    public QueryBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
