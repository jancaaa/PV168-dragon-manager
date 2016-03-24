package cz.muni.fi.pv168;

import cz.muni.fi.pv168.common.DBUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;



import java.sql.SQLException;
import java.util.*;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author: Jana Zahradnickova,  UCO 433598
 * @version: 24. 3. 2016
 */
public class LeaseManagerTest {
    private LeaseManagerImpl manager;
    private DataSource dataSource;

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        DBUtils.executeSqlScript(dataSource, LeaseManager.class.getResource("db_create.sql"));
        manager = new LeaseManagerImpl(dataSource);
    }

    private DataSource prepareDataSource() {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        //we will use in memory database
        ds.setDatabaseName("memory:leasemgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(dataSource, LeaseManager.class.getResource("db_drop.sql"));
    }

    @Test
    public void createLease() {
        Customer customer = newCustomer("Ashen Shugar", "+999 666 333", "Iratus 7");
        Dragon dragon = newDragon("janca's dragon", 3, 100);
        Lease lease = newLease(dragon,customer,new Date(2016,2,2),new Date(2016,2,3),200);
        manager.createLease(lease);

        Long leaseId = lease.getId();
        assertThat("saved lease has null id", lease.getId(), is(not(equalTo(null))));


        Lease result = manager.getLease(leaseId);
        //loaded instance should be equal to the saved one
        assertThat("loaded lease different from the saved one", result, is(equalTo(lease)));
        //but it should be another instance
        assertThat("loaded lease is the same instance", result, is(not(sameInstance(lease))));
        //equals method test
        assertDeepEquals(lease, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDragonWithNull() throws Exception {
        manager.createLease(null);
    }

    @Test
    public void createLeaseWithWrongValues() {

        Customer customer = newCustomer("Ashen Shugar", "+999 666 333", "Iratus 7");
        Dragon dragon = newDragon("janca's dragon", 3, 100);
        Lease lease = newLease(dragon,customer,new Date(2016,2,2),new Date(2016,2,3),200);
        lease.setId(1L);
        try {
            manager.createLease(lease);
            fail("should refuse assigned id");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        lease = newLease(dragon,customer,new Date(2017,2,29),new Date(2016,2,3),200);
        try {
            manager.createLease(lease);
            fail("endDate before startDate");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        lease = newLease(dragon,customer,new Date(2016,2,2),new Date(2016,2,3),-200);
        try {
            manager.createLease(lease);
            fail("price is negative");
        } catch (IllegalArgumentException ex) {
            //OK
        }
    }

    @Test
    public void getAllLeases() {

        assertTrue(manager.findAllLeases().isEmpty());
        Customer customer1 = newCustomer("Ashen Shugar", "+999 666 333", "Iratus 7");
        Dragon dragon1 = newDragon("janca's dragon", 3, 100);
        Customer customer2 = newCustomer("Petr Neznamy","+333 666 999","Horni Dolni");
        Dragon dragon2 = newDragon("janca's second dragon", 5, 200);

        Lease l1 = newLease(dragon1,customer1,new Date(2016,2,2),new Date(2016,2,3),200);
        Lease l2 = newLease(dragon2,customer2,new Date(2016,5,5),new Date(2016,5,6),200);

        manager.createLease(l1);
        manager.createLease(l2);

        List<Lease> expected = Arrays.asList(l1, l2);
        List<Lease> actual = manager.findAllLeases();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals("saved and retrieved leases differ", expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void updateLease() {
        Customer customer1 = newCustomer("Ashen Shugar", "+999 666 333", "Iratus 7");
        Dragon dragon1 = newDragon("janca's dragon", 3, 100);
        Customer customer2 = newCustomer("Petr Neznamy","+333 666 999","Horni Dolni");
        Dragon dragon2 = newDragon("janca's second dragon", 5, 200);


        Customer customer3 = newCustomer("Petr Znamy","+333 666 999","Dolni Horni");
        Dragon dragon3 = newDragon("janca's thord dragon", 10, 1000);

        Lease lease = newLease(dragon1,customer1,new Date(2016,2,2),new Date(2016,2,3),200);
        Lease secondLease = newLease(dragon2,customer2,new Date(2016,5,5),new Date(2016,5,6),200);

        manager.createLease(lease);
        manager.createLease(secondLease);
        Long leaseId = lease.getId();

        //change of customer
        lease.setCustomer(customer3);
        manager.updateLease(lease);
        //load from database
        lease = manager.getLease(leaseId);

        assertThat("customer was not changed", lease.getCustomer(), is(equalTo(customer3)));
        assertThat("dragon was changed when changing customer", lease.getDragon(), is(equalTo(dragon1)));
        assertThat("startDate was changed when changing customer", lease.getStartDate(), is(equalTo(new Date(2016,2,2))));
        assertThat("endDate was changed when changing customer", lease.getEndDate(), is(equalTo(new Date(2016,2,3))));
        assertThat("price was changed when changing customer", lease.getPrice(), is(equalTo(200)));

        //change of dragon
        lease.setDragon(dragon3);
        manager.updateLease(lease);
        //load from database
        lease = manager.getLease(leaseId);

        assertThat("dragon was not changed", lease.getDragon(), is(equalTo(dragon3)));
        assertThat("customer was changed when changing dragon", lease.getCustomer(), is(equalTo(customer1)));
        assertThat("startDate was changed when changing dragon", lease.getStartDate(), is(equalTo(new Date(2016,2,2))));
        assertThat("endDate was changed when changing dragon", lease.getEndDate(), is(equalTo(new Date(2016,2,3))));
        assertThat("price was changed when changing dragon", lease.getPrice(), is(equalTo(200)));

        //change of startDate
        lease.setStartDate(new Date(2010,1,1));
        manager.updateLease(lease);
        lease = manager.getLease(leaseId);

        assertThat("startDate was not changed", lease.getStartDate(), is(equalTo(new Date(2010,1,1))));
        assertThat("dragon was changed when changing startDate", lease.getDragon(), is(equalTo(dragon1)));
        assertThat("customer was changed when changing startDate", lease.getCustomer(), is(equalTo(customer1)));
        assertThat("endDate was changed when changing startDate", lease.getEndDate(), is(equalTo(new Date(2016,2,3))));
        assertThat("price was changed when changing startDate", lease.getPrice(), is(equalTo(200)));

        //change of endDate
        lease.setEndDate(new Date(2020,1,1));
        manager.updateLease(lease);
        lease = manager.getLease(leaseId);

        assertThat("endDate was not changed", lease.getEndDate(), is(equalTo(new Date(2020,1,1))));
        assertThat("dragon was changed when changing endDate", lease.getDragon(), is(equalTo(dragon1)));
        assertThat("customer was changed when changing endDate", lease.getCustomer(), is(equalTo(customer1)));
        assertThat("startDate was changed when changing endDate", lease.getStartDate(), is(equalTo(new Date(2016,2,2))));
        assertThat("price was changed when changing endDate", lease.getPrice(), is(equalTo(200)));

        //change of price

        lease.setPrice(5000);
        manager.updateLease(lease);
        lease = manager.getLease(leaseId);

        assertThat("price was not changed", lease.getPrice(), is(equalTo(5000)));
        assertThat("dragon was changed when changing countOfHeads", lease.getDragon(), is(equalTo(dragon1)));
        assertThat("customer was changed when changing countOfHeads", lease.getCustomer(), is(equalTo(customer1)));
        assertThat("startDate was changed when changing countOfHeads", lease.getStartDate(), is(equalTo(new Date(2016,2,2))));
        assertThat("endDate was changed when changing countOfHeads", lease.getEndDate(), is(equalTo(new Date(2016,2,3))));


        // Check if updates didn't affected other records
        assertDeepEquals(secondLease, manager.getLease(secondLease.getId()));
    }

    @Test
    public void updateLeaseWithWrongAttributes() {

        Customer customer = newCustomer("Ashen Shugar", "+999 666 333", "Iratus 7");
        Dragon dragon = newDragon("janca's dragon", 3, 100);
        Lease lease = newLease(dragon,customer,new Date(2016,2,2),new Date(2016,2,3),200);
        manager.createLease(lease);
        Long dragonId = lease.getId();

        try {
            manager.updateLease(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            lease = manager.getLease(dragonId);
            long id = Long.parseLong(null);
            lease.setId(id);
            manager.updateLease(lease);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            lease = manager.getLease(dragonId);
            lease.setId(dragonId - 1);
            manager.updateLease(lease);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            lease = manager.getLease(dragonId);
            lease.setDragon(null);
            manager.updateLease(lease);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            lease = manager.getLease(dragonId);
            lease.setCustomer(null);
            manager.updateLease(lease);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            lease = manager.getLease(dragonId);
            lease.setPrice(-1);
            manager.updateLease(lease);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
/*
        try {
            lease = manager.getLease(dragonId);
            lease.setStartDate(new Date(2020,1,1));
            manager.updateLease(lease);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            lease = manager.getLease(dragonId);
            lease.setEndDate(new Date(2010,1,1));
            manager.updateLease(lease);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        */
    }

    @Test
    public void deleteLease() {

        Customer customer1 = newCustomer("Ashen Shugar", "+999 666 333", "Iratus 7");
        Dragon dragon1 = newDragon("janca's dragon", 3, 100);
        Customer customer2 = newCustomer("Petr Neznamy","+333 666 999","Horni Dolni");
        Dragon dragon2 = newDragon("janca's second dragon", 5, 200);

        Lease lease1 = newLease(dragon1,customer1,new Date(2016,2,2),new Date(2016,2,3),200);
        Lease lease2 = newLease(dragon2,customer2,new Date(2016,5,5),new Date(2016,5,6),200);

        manager.createLease(lease1);
        manager.createLease(lease2);

        assertNotNull(manager.getLease(lease1.getId()));
        assertNotNull(manager.getLease(lease2.getId()));

        manager.deleteLease(lease1);

        assertNull(manager.getLease(lease1.getId()));
        assertNotNull(manager.getLease(lease2.getId()));
    }

    @Test
    public void deleteLeaseWithWrongAttributes() {

        Customer customer = newCustomer("Ashen Shugar", "+999 666 333", "Iratus 7");
        Dragon dragon = newDragon("janca's dragon", 3, 100);
        Lease lease = newLease(dragon,customer,new Date(2016,2,2),new Date(2016,2,3),200);

        try {
            manager.deleteLease(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            long id = Long.parseLong(null);
            lease.setId(id);
            manager.deleteLease(lease);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            lease.setId(1L);
            manager.deleteLease(lease);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
    }

    private void assertDeepEquals(List<Lease> expectedList, List<Lease> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Lease expected = expectedList.get(i);
            Lease actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(Lease expected, Lease actual) {
        assertEquals("id value is not equal", expected.getId(), actual.getId());
        assertEquals("dragon value is not equal", expected.getDragon(), actual.getDragon());
        assertEquals("customer value is not equal", expected.getCustomer(), actual.getCustomer());
        assertEquals("startDate value is not equal", expected.getStartDate(), actual.getEndDate());
        assertEquals("endDate value is not equal", expected.getEndDate(), actual.getEndDate());
    }

    private static Lease newLease(Dragon dragon, Customer customer, Date startDate, Date endDate, int price) {
        Lease lease = new Lease();

        lease.setDragon(dragon);
        lease.setCustomer(customer);
        lease.setStartDate(startDate);
        lease.setEndDate(endDate);
        return lease;
    }

    private static Dragon newDragon(String name, int countOfHeads, int priceForDay) {
        Dragon dragon = new Dragon();

        //   dragon.setId();
        dragon.setName(name);
        dragon.setCountOfHeads(countOfHeads);
        dragon.setPriceForDay(priceForDay);
        return dragon;
    }

    private static Customer newCustomer(String name, String phone, String address) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        customer.setAddress(address);
        return customer;
    }

    private static Comparator<Lease> idComparator = new Comparator<Lease>() {
        @Override
        public int compare(Lease o1, Lease o2) {
            return o1.getId().compareTo(o2.getId());
        }

    };
}
