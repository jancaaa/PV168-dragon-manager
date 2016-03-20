package cz.muni.fi.pv168;

/**
 * @author: Jana Zahradnickova,  UCO 433598
 * @version: 13. 3. 2016
 */
public class ServiceFailureException extends RuntimeException {
    public ServiceFailureException(String msg) {
        super(msg);
    }

    public ServiceFailureException(Throwable cause) {
        super(cause);
    }

    public ServiceFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
