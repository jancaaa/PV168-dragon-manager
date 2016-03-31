package cz.muni.fi.pv168;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Tomas Sakal,  UCO 433690
 * @version: 28. 3. 2016
 */
public class CustomerManagerImpl implements CustomerManager {

    final static Logger log = LoggerFactory.getLogger(CustomerManagerImpl.class);

    private final DataSource dataSource;

    public CustomerManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createCustomer(Customer customer) throws ServiceFailureException {
        if (customer == null) {
            throw new IllegalArgumentException("customer is null");
        } else if (customer.getId() != null) {
            throw new IllegalArgumentException("customer id is already set");
        } else if (customer.getName().length() < 1) {
            throw new IllegalArgumentException("customer name length < 1");
        } else if (customer.getPhone().length() < 1) {
            throw new IllegalArgumentException("customer phone number length < 1 ");
        } else if (customer.getAddress().length() < 1) {
            throw new IllegalArgumentException("customer address length < 1 ");
        }

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO customer (name,phone,address) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, customer.getName());
                st.setString(2, customer.getPhone());
                st.setString(3, customer.getAddress());
                int addedRows = st.executeUpdate();
                if (addedRows != 1) {
                    throw new ServiceFailureException("Internal Error: More rows inserted when trying to " +
                            "insert customer " + customer);
                }
                ResultSet keyRS = st.getGeneratedKeys();
                customer.setId(getKey(keyRS, customer));
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when creating a customer", ex);
        }

    }

    private Long getKey(ResultSet keyRS, Customer customer) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert customer " + customer
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert customer " + customer
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retrieving failed when trying to insert customer " + customer
                    + " - no key found");
        }
    }

    @Override
    public Customer getCustomer(Long id) throws ServiceFailureException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,name,phone,address FROM customer WHERE id = ?")) {
                st.setLong(1, id);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    Customer customer = resultSetToCustomer(rs);
                    if (rs.next()) {
                        throw new ServiceFailureException(
                                "Internal error: More entities with the same id found "
                                        + "(source id: " + id + ", found " + customer + " and " + resultSetToCustomer(rs));
                    }
                    return customer;
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when getting a customer", ex);
        }
    }

    private Customer resultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getLong("id"));
        customer.setName(rs.getString("name"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));
        return customer;
    }

    @Override
    public void updateCustomer(Customer customer) throws ServiceFailureException {
        if (customer == null) throw new IllegalArgumentException("customer pointer is null");
        if (customer.getId() == null) throw new IllegalArgumentException("customer with a null id cannot be updated");
        if (customer.getPhone().length() < 1)
            throw new IllegalArgumentException("customer phone number length is not a positive number");
        if (customer.getAddress().length() < 1) throw new IllegalArgumentException("customer address length " +
                "is not a positive number");

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("UPDATE customer SET name=?,phone=?,address=? " +
                    "WHERE id=?")) {
                st.setString(1, customer.getName());
                st.setString(2, customer.getPhone());
                st.setString(3, customer.getAddress());
                st.setLong(4, customer.getId());
                if (st.executeUpdate() != 1) {
                    throw new IllegalArgumentException("cannot update customer " + customer);
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when updating a customer", ex);
        }
    }

    @Override
    public void deleteCustomer(Customer customer) throws ServiceFailureException {
        if (customer == null)
            throw new IllegalArgumentException("customer pointer is null");
        if (customer.getId() == null)
            throw new IllegalArgumentException("customer with null id cannot be deleted");

        if (getCustomer(customer.getId()) == null)
            throw new IllegalArgumentException("customer with given id does not exist");

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM customer WHERE id=?")) {
                st.setLong(1, customer.getId());
                if (st.executeUpdate() != 1) {
                    throw new ServiceFailureException("did not delete customer with id =" + customer);
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when deleting a customer", ex);
        }
    }

    @Override
    public List<Customer> findAllCustomers() throws ServiceFailureException {
        log.debug("finding all customers");
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,name,phone,address FROM customer")) {
                ResultSet rs = st.executeQuery();
                List<Customer> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToCustomer(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all customers", ex);
        }
    }
}

