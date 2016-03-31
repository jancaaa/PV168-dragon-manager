package cz.muni.fi.pv168;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

/**
 * @author: Tomas Sakal,  UCO 433690
 * @version: 28. 3. 2016
 */
public class LeaseManagerImpl implements LeaseManager {

    final static Logger log = LoggerFactory.getLogger(CustomerManagerImpl.class);

    private final DataSource dataSource;

    public LeaseManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createLease(Lease lease) throws ServiceFailureException {
       validate(lease);
        if (lease.getId() != null)
            throw new IllegalArgumentException("lease id is already set");

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO lease (dragon,customer,startdate,enddate,price) " +
                    "VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                st.setLong(1, lease.getDragon());
                st.setLong(2, lease.getCustomer());

                st.setDate(3, Date.valueOf(lease.getStartDate()));
                st.setDate(4, Date.valueOf(lease.getEndDate()));
                st.setInt(5, lease.getPrice());
                int addedRows = st.executeUpdate();
                if (addedRows != 1) {
                    throw new ServiceFailureException("Internal Error: More rows inserted when trying to " +
                            "insert lease " + lease);
                }
                ResultSet keyRS = st.getGeneratedKeys();
                lease.setId(getKey(keyRS, lease));
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when creating a lease", ex);
        }

    }

    private Long getKey(ResultSet keyRS, Lease lease) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert lease " + lease
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert lease " + lease
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retrieving failed when trying to insert lease " + lease
                    + " - no key found");
        }
    }

    @Override
    public Lease getLease(Long id) throws ServiceFailureException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,dragon,customer,startdate,enddate,price " +
                    "FROM lease WHERE id = ?")) {
                st.setLong(1, id);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    Lease lease = resultSetToLease(rs);
                    if (rs.next()) {
                        throw new ServiceFailureException(
                                "Internal error: More entities with the same id found "
                                        + "(source id: " + id + ", found " + lease + " and " + resultSetToLease(rs));
                    }
                    return lease;
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when getting a lease", ex);
        }
    }

    private Lease resultSetToLease(ResultSet rs) throws SQLException {
        Lease lease = new Lease();
        lease.setId(rs.getLong("id"));
        lease.setDragon(rs.getLong("dragon"));
        lease.setCustomer(rs.getLong("customer"));
        lease.setStartDate(rs.getDate("startdate").toLocalDate());
        lease.setEndDate(rs.getDate("enddate").toLocalDate());
        lease.setPrice(rs.getInt("price"));
        return lease;
    }

    @Override
    public void updateLease(Lease lease) throws ServiceFailureException {
       validate(lease);

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("UPDATE lease SET dragon=?,customer=?,startdate=?,enddate=?,price=? "
                    + "WHERE id=?")) {
                st.setLong(1, lease.getDragon());
                st.setLong(2, lease.getCustomer());
                st.setDate(3, Date.valueOf(lease.getStartDate()));
                st.setDate(4, Date.valueOf(lease.getEndDate()));
                st.setInt(5, lease.getPrice());
                st.setLong(6, lease.getId());
                if (st.executeUpdate() != 1) {
                    throw new IllegalArgumentException("cannot update lease " + lease);
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when updating a lease", ex);
        }
    }

    @Override
    public void deleteLease(Lease lease) throws ServiceFailureException {
        if (lease == null)
            throw new IllegalArgumentException("lease pointer is null");
        if (lease.getId() == null)
            throw new IllegalArgumentException("lease with null id cannot be deleted");

        if (getLease(lease.getId()) == null)
            throw new IllegalArgumentException("lease with given id does not exist");

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM lease WHERE id=?")) {
                st.setLong(1, lease.getId());
                if (st.executeUpdate() != 1) {
                    throw new ServiceFailureException("did not delete lease with id =" + lease);
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when deleting a lease", ex);
        }
    }

    @Override
    public List<Lease> findAllLeases() throws ServiceFailureException {
        log.debug("finding all dragons");
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,dragon,customer,startdate,enddate,price FROM lease")) {
                ResultSet rs = st.executeQuery();
                List<Lease> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToLease(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all leases", ex);
        }
    }

    private void validate(Lease lease){
        if (lease == null) {
            throw new IllegalArgumentException("lease is null");
        } else if (lease.getDragon() == null) {
            throw new IllegalArgumentException("dragon is null");
        } else if (lease.getCustomer()  == null) {
            throw new IllegalArgumentException("customer is null");
        } else if (lease.getStartDate()  == null) {
            throw new IllegalArgumentException("start date is null");
        } else if (lease.getEndDate()  == null) {
            throw new IllegalArgumentException("end date is null");
        } else if (lease.getStartDate().isAfter(lease.getEndDate())){
            throw new IllegalArgumentException("start date is after end date");
        } else if (lease.getPrice() < 0) {
            throw new IllegalArgumentException("lease price < 0 ");
        }
    }
}
