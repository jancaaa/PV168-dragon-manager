package cz.muni.fi.pv168;

/**
 * This exception is thrown when a delete or an update operation is performed
 * with an entity that does not exist in the DB.
 *
 * Created by Tom on marec 22, 2016.
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * Constructs an instance of the with the specified detail message.
     * @param msg the detail message.
     */
    public EntityNotFoundException(String msg) {
        super(msg);
    }
}
