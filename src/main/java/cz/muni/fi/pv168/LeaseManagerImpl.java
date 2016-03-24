package cz.muni.fi.pv168;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author: Jana Zahradnickova,  UCO 433598
 * @version: 8. 3. 2016
 */
public class LeaseManagerImpl implements LeaseManager {
    final static Logger log = LoggerFactory.getLogger(DragonManagerImpl.class);

    private final DataSource dataSource;

    public LeaseManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createLease(Lease lease) throws ServiceFailureException {

    }

    @Override
    public Lease getLease(Long id) throws ServiceFailureException {
        return null;
    }

    @Override
    public void updateLease(Lease lease) throws ServiceFailureException {

    }

    @Override
    public void deleteLease(Lease lease) throws ServiceFailureException {

    }

    @Override
    public List<Lease> findAllLeases() throws ServiceFailureException {
        return null;
    }
}
