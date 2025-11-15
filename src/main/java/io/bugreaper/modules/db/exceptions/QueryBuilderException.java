package io.bugreaper.modules.db.exceptions;

public class QueryBuilderException extends RuntimeException {

    public QueryBuilderException(String message) {
        super(message);
    }
    public QueryBuilderException(String message, Throwable cause) {
        super(message, cause);
    }
}
