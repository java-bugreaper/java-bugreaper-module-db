package net.bugreaper.modules.db.exceptions;

/**
 * Exceptions that can be thrown during SqlExecute use
 */
public class SqlExecuteException extends RuntimeException {

    /**
     * Constructs a new exception with the specified cause
     *
     * @param cause the cause
     */
    public SqlExecuteException(Throwable cause) {
        super(cause);
    }

}
