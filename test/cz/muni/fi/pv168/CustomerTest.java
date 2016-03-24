package cz.muni.fi.pv168;

import cz.muni.fi.pv168.common.DBUtils;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

/**
 * spravne nastavit cizi klice. integritni omezeni
 * nepisat vysvetlovacie hlasky do testov - vyhadzat s assertov
 * *  su deterministicke, daju sa hocikedy zopakovat
 * *  pri uprave testu budem musiet upravovat text hlasky
 * vyjimky by mali byt dobre popisane
 * mozme pouzit expected exception (jednoriadkovy test)
 * transakce - mnozina operaci nad databazi ACID ktere se provedou atomicky (!)
 * *  po jejich provedeni jsou data ulozena v databazi poradne
 * vlakna transakcie - nemozeme to robit nad rovnakym spojenim simultanne
 * nad rovnakym spojenim konzekutivne transakce
 * na ulohu nam staci jedno spojenie na rozdiel od stvrtej
 * automaticke spojenie - autocommit rezim
 *
 *  *  loosely coupled once interface is added
 *  nullpointerexc for dereferencing null
 *  illegalargexc method with an invalid argument
 *  unchecked exc is for programmer failure - contract breach (illegalarg, unsupportedoperation, illegalstate)
 *  generalization-specialization relationship in inheritance
 *  inheritance: fixed relationship (student can't become a teacher, they're both people)
 *  *  instead, aggregation and delegation can be used
 *  unsupportedopexc pre zatial neimplementovane metody
 *  aby presli testy treba napojit databazu
 */

/**
 * Created by Tom on marec 15, 2016.
 */
public class CustomerTest {

    private CustomerManagerImpl manager;
    private DataSource dataSource;

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        DBUtils.executeSqlScript(dataSource, LeaseManager.class.getResource("db_create.sql"));
        manager = new CustomerManagerImpl(dataSource);
    }

    private DataSource prepareDataSource() {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        //we will use in memory database
        ds.setDatabaseName("memory:customermgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(dataSource, CustomerManager.class.getResource("db_drop.sql"));
    }


    @Test
    public void createCustomer() {
        Customer customer = newCustomer("Ashen Shugar", "+999 666 333", "Iratus 7");
        manager.createCustomer(customer);

        Long customerId = customer.getId();
        assertThat("saved customer has null id", customer.getId(), is(not(equalTo(null))));

        Customer result = manager.getCustomer(customerId);
        assertThat("loaded customer differs from the saved one", result, is(equalTo(customer)));
        assertDeepEquals(customer, result);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testCreateWithNull() {
        manager.createCustomer(null);
    }

    @Test
    public void createCustomerWithWrongValuesId() {
        Customer customer = newCustomer("Ashen Shugar", "+999 666 333", "Iratus 7");
        customer.setId(5L);
        try {
            manager.createCustomer(customer);
            fail("ought to refuse assigned id");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void createCustomerWithWrongValuesNum() {
        Customer customer = newCustomer("Ashen Shugar", "alphabeta", "Iratus 7");
        try {
            manager.createCustomer(customer);
            fail("alphabetical phone number not allowed");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void createCustomerWithWrongValuesName() {
        Customer customer = newCustomer("123 456", "+999 666 333", "Iratus 7");
        try {
            manager.createCustomer(customer);
            fail("numerical name not allowed");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void getAllCustomers() {

        assertTrue(manager.findAllCustomers().isEmpty());

        Customer c1 = newCustomer("a b", "+421 111", "abc 5");
        Customer c2 = newCustomer("c d", "+420 222", "def 6");

        manager.createCustomer(c1);
        manager.createCustomer(c2);

        List<Customer> expected = Arrays.asList(c1, c2);
        List<Customer> actual = manager.findAllCustomers();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals("saved and retrieved customers differ", expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void deleteCustomer() {

        Customer c1 = newCustomer("a b", "+421 111", "abc 5");
        Customer c2 = newCustomer("c d", "+420 222", "def 6");
        manager.createCustomer(c1);
        manager.createCustomer(c2);

        assertNotNull(manager.getCustomer(c1.getId()));
        assertNotNull(manager.getCustomer(c2.getId()));

        manager.deleteCustomer(c1);

        assertNull(manager.getCustomer(c1.getId()));
        assertNotNull(manager.getCustomer(c2.getId()));
    }

    @Test
    public void deleteCustomerWithWrongAttributesNull() {
        try {
            manager.deleteCustomer(null);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void deleteCustomerWithWrongAttributesId() {
        Customer customer = newCustomer("Ashen Shugar", "+999 666 333", "Iratus 7");

        try {
            customer.setId(2L);
            manager.deleteCustomer(customer);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    private static Customer newCustomer(String name, String phone, String address) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        customer.setAddress(address);
        return customer;
    }

    private void assertDeepEquals(List<Customer> expectedList, List<Customer> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Customer expected = expectedList.get(i);
            Customer actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(Customer expected, Customer actual) {
        assertEquals("id value is not equal",expected.getId(), actual.getId());
        assertEquals("name is not equal",expected.getName(), actual.getName());
        assertEquals("phone is not equal",expected.getPhone(), actual.getPhone());
        assertEquals("address is not equal",expected.getAddress(), actual.getAddress());
    }

    private static Comparator<Customer> idComparator = new Comparator<Customer>() {
        @Override
        public int compare(Customer o1, Customer o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };


}