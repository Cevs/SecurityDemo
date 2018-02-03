package com.example.SecurityDemo.exceptions;

public final class IpBanException extends RuntimeException {
    public IpBanException() {
        super();
    }

    public IpBanException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public IpBanException(final String message) {
        super(message);
    }

    public IpBanException(final Throwable cause) {
        super(cause);
    }
}
