package cz.muni.fi.pv168;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jana Zahradnickova,  UCO 433598
 * @version: 8. 3. 2016
 */
public class CustomerManagerImpl implements CustomerManager {

    private final DataSource dataSource;

    public CustomerManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void validate(Customer customer) throws IllegalArgumentException {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is null");
        }
        if (customer.getName() == null) {
            throw new IllegalArgumentException("Customer name is null pointer");
        }
        if (customer.getName().length() == 0) {
            throw new IllegalArgumentException("Customer name length is zero");
        }

        if (customer.getPhone() == null) {
            throw new IllegalArgumentException("Customer phone length is null pointer");
        }
        if (customer.getPhone().length() == 0) {
            throw new IllegalArgumentException("Customer phone length is zero");
        }

        if (customer.getAddress() == null) {
            throw new IllegalArgumentException("Customer address is null pointer");
        }
        if (customer.getAddress().length() == 0) {
            throw new IllegalArgumentException("Customer address length is zero");
        }
    }

    @Override
    public void createCustomer(Customer Customer) throws ServiceFailureException {

        validate(Customer);

        if (Customer.getId() != null) {
            throw new IllegalArgumentException("Customer id is already set");
        }

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "INSERT INTO customer (name,phone,address) VALUES (?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            st.setString(1, Customer.getName());
            st.setString(2, Customer.getPhone());
            st.setString(3, Customer.getAddress());

            ResultSet keyRS = st.getGeneratedKeys();
            Customer.setId(getKey(keyRS, Customer));

        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting customer " + Customer, ex);
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
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id,name,phone,address FROM customer WHERE id = ?")) {

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

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving customer with id " + id, ex);
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
        validate(customer);
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer id is null");
        }
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "UPDATE customer SET name = ?, phone = ?, address = ? WHERE id = ?")) {

            st.setString(1, customer.getName());
            st.setString(2, customer.getPhone());
            st.setString(3, customer.getAddress());
            st.setLong(4, customer.getId());

            int count = st.executeUpdate();
            if (count == 0) {
                throw new EntityNotFoundException("Customer " + customer + " was not found in database!");
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when updating customer " + customer, ex);
        }
    }

    @Override
    public void deleteCustomer(Customer customer) throws ServiceFailureException {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is null");
        }
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer id is null");
        }

        if (getCustomer(customer.getId())==null)
            throw new IllegalArgumentException("Customer with given id does not exists");

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM cutomer WHERE id=?")) {
                st.setLong(1, customer.getId());
                if (st.executeUpdate() != 1) {
                    throw new ServiceFailureException("did not delete customer with id =" + customer);
                }
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when retrieving all dragons", ex);
        }
    }

    @Override
    public List<Customer> findAllCustomers() throws ServiceFailureException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id,name,phone,address FROM customer")) {

            ResultSet rs = st.executeQuery();

            List<Customer> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToCustomer(rs));
            }
            return result;

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving all customers", ex);
        }
    }

}