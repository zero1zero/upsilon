package com.vevo.upsilon.except;

public class UpsilonUpgradeException extends RuntimeException {

    public UpsilonUpgradeException(String message) {
        super(message);
    }

    public UpsilonUpgradeException(String message, Throwable cause) {
        super(message, cause);
    }
}
