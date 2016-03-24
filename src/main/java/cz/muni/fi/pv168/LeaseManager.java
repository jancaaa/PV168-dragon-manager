package cz.muni.fi.pv168;

import java.util.List;

/**
 * @author: Jana Zahradnickova,  UCO 433598
 * @version: 8. 3. 2016
 */
public interface LeaseManager {
    void createLease(Lease lease) throws ServiceFailureException;
    Lease getLease(Long id) throws ServiceFailureException;
    void updateLease(Lease lease) throws ServiceFailureException;
    void deleteLease(Lease lease) throws ServiceFailureException;
    List<Lease> findAllLeases() throws ServiceFailureException;
}
