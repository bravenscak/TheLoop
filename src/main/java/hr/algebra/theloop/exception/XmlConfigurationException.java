package hr.algebra.theloop.exception;

public class XmlConfigurationException extends Exception {

    public XmlConfigurationException() {
        super();
    }

    public XmlConfigurationException(String message) {
        super(message);
    }

    public XmlConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public XmlConfigurationException(Throwable cause) {
        super(cause);
    }

    public XmlConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}