package com.gptlambda.api.exception;

import lombok.Getter;

/**
 * @author Sergey Golitsyn
 * created on 29.12.2021
 * <p>
 * Basic Kippo exception.
 */
@Getter
public class GPTLambdaException extends RuntimeException {
    private String title;
    private String errMessage;

    public GPTLambdaException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public GPTLambdaException(String errMessage, Exception ex) {
        super(errMessage, ex);
    }

    public GPTLambdaException(Exception ex) {
        super(ex);
    }
}
