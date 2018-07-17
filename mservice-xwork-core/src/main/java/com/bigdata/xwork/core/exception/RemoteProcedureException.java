package com.bigdata.xwork.core.exception;

/**
 * 远程调用错误
 */
public class RemoteProcedureException extends Exception  {
    private static final long serialVersionUID = 1L;

    public RemoteProcedureException(String message) {
        super(message);
    }
}
