package cz.muni.fi.pv168;

import java.util.List;

/**
 * @author: Jana Zahradnickova,  UCO 433598
 * @version: 8. 3. 2016
 */
public interface CustomerManager {

    void createCustomer(Customer customer) throws ServiceFailureException;

    Customer getCustomer(Long id) throws ServiceFailureException;

    void updateCustomer(Customer customer) throws ServiceFailureException;

    void deleteCustomer(Customer customer) throws ServiceFailureException;

    List<Customer> findAllCustomers() throws ServiceFailureException;
}
