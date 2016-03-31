package cz.muni.fi.pv168;

import java.util.List;

/**
 * @author: Jana Zahradnickova,  UCO 433598
 * @version: 8. 3. 2016
 */
public interface DragonManager {

    void createDragon(Dragon dragon) throws ServiceFailureException;

    Dragon getDragon(Long id) throws ServiceFailureException;

    void updateDragon(Dragon dragon) throws ServiceFailureException;

    void deleteDragon(Dragon dragon) throws ServiceFailureException;

    List<Dragon> findAllDragon() throws ServiceFailureException;
}
