package cz.muni.fi.pv168;

import cz.muni.fi.pv168.common.DBUtils;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

import java.sql.Date;
import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author: Jana Zahradnickova,  UCO 433598
 * @version: 24. 3. 2016
 */
public class LeaseManagerTest {
    private LeaseManagerImpl manager;
    private DragonManagerImpl dragonManager;
    private CustomerManagerImpl customerManager;
    private DataSource dataSource;

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        DBUtils.executeSqlScript(dataSource, LeaseManager.class.getResource("db_create.sql"));

        manager = new LeaseManagerImpl(dataSource);
        dragonManager = new DragonManagerImpl(dataSource);
        customerManager = new CustomerManagerImpl(dataSource);
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
        customerManager.createCustomer(customer);
        Dragon dragon = newDragon("janca's dragon", 3, 100);
        dragonManager.createDragon(dragon);

        LocalDate start = LocalDate.of(2016,Month.FEBRUARY,2);
        LocalDate end = LocalDate.of(2016,Month.FEBRUARY,3);

        Lease lease = newLease(dragon,customer,start,end,200);
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

        LocalDate start = LocalDate.of(2016,Month.FEBRUARY,2);
        LocalDate end = LocalDate.of(2016,Month.FEBRUARY,3);
        Lease lease = newLease(dragon,customer,start,end,200);
        lease.setId(1L);
        try {
            manager.createLease(lease);
            fail("should refuse assigned id");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        LocalDate start1 = LocalDate.of(2017,Month.FEBRUARY,28);
        LocalDate end1 = LocalDate.of(2016,Month.FEBRUARY,3);
        lease = newLease(dragon,customer,start1,end1,200);
        try {
            manager.createLease(lease);
            fail("endDate before startDate");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        LocalDate start2 = LocalDate.of(2016,Month.FEBRUARY,2);
        LocalDate end2 = LocalDate.of(2016,Month.FEBRUARY,3);
        lease = newLease(dragon,customer,start2,end2,-200);
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

        dragonManager.createDragon(dragon1);
        dragonManager.createDragon(dragon2);
        customerManager.createCustomer(customer1);
        customerManager.createCustomer(customer2);

        LocalDate start = LocalDate.of(2016,Month.FEBRUARY,2);
        LocalDate end = LocalDate.of(2016,Month.FEBRUARY,3);
        Lease l1 = newLease(dragon1,customer1,start,end,200);
        LocalDate start1 = LocalDate.of(2016,Month.MAY,5);
        LocalDate end1 = LocalDate.of(2016,Month.MAY,6);
        Lease l2 = newLease(dragon2,customer2,start1,end1,200);

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

        dragonManager.createDragon(dragon1);
        dragonManager.createDragon(dragon2);
        customerManager.createCustomer(customer1);
        customerManager.createCustomer(customer2);


        LocalDate start1 = LocalDate.of(2016, Month.FEBRUARY,2);
        LocalDate end1 = LocalDate.of(2016, Month.FEBRUARY,3);
        LocalDate start2 = LocalDate.of(2016,Month.MAY,5);
        LocalDate end2 = LocalDate.of(2016,Month.MAY,6);


        Lease lease1 = newLease(dragon1,customer1,start1,end1,200);
        Lease lease2 = newLease(dragon2,customer2,start2,end2,200);

        manager.createLease(lease1);
        manager.createLease(lease2);

        Customer customer3 = newCustomer("Petr Znamy","+333 666 999","Dolni Horni");
        Dragon dragon3 = newDragon("janca's third dragon", 10, 1000);

        dragonManager.createDragon(dragon3);
        customerManager.createCustomer(customer3);

        Long leaseId = lease1.getId();

        //change of customer
        lease1.setCustomer(customer3.getId());
        manager.updateLease(lease1);
        //load from database
        lease1 = manager.getLease(leaseId);

        assertThat("customer was not changed", lease1.getCustomer(), is(equalTo(customer3.getId())));
        assertThat("dragon was changed when changing customer", lease1.getDragon(), is(equalTo(dragon1.getId())));
        assertThat("startDate was changed when changing customer", lease1.getStartDate(), is(equalTo(start1)));
        assertThat("endDate was changed when changing customer", lease1.getEndDate(), is(equalTo(end1)));
        assertThat("price was changed when changing customer", lease1.getPrice(), is(equalTo(200)));

        //change of dragon
        lease1.setDragon(dragon3.getId());
        manager.updateLease(lease1);
        //load from database
        lease1 = manager.getLease(leaseId);


        assertThat("dragon was not changed", lease1.getDragon(), is(equalTo(dragon3.getId())));
        assertThat("customer was changed when changing dragon", lease1.getCustomer(), is(equalTo(customer3.getId())));
        assertThat("startDate was changed when changing dragon", lease1.getStartDate(), is(equalTo(start1)));
        assertThat("endDate was changed when changing dragon", lease1.getEndDate(), is(equalTo(end1)));
        assertThat("price was changed when changing dragon", lease1.getPrice(), is(equalTo(200)));

        //change of startDate
        LocalDate start3 =  LocalDate.of(2010,Month.JANUARY,1);
        lease1.setStartDate(start3);
        manager.updateLease(lease1);
        lease1 = manager.getLease(leaseId);


        assertThat("startDate was not changed", lease1.getStartDate(), is(equalTo(start3)));
        assertThat("dragon was changed when changing startDate", lease1.getDragon(), is(equalTo(dragon3.getId())));
        assertThat("customer was changed when changing startDate", lease1.getCustomer(), is(equalTo(customer3.getId())));
        assertThat("endDate was changed when changing startDate", lease1.getEndDate(), is(equalTo(end1)));
        assertThat("price was changed when changing startDate", lease1.getPrice(), is(equalTo(200)));

        //change of endDate
        LocalDate end3 = LocalDate.of(2020,Month.JANUARY,1);
        lease1.setEndDate(end3);
        manager.updateLease(lease1);
        lease1 = manager.getLease(leaseId);

        assertThat("endDate was not changed", lease1.getEndDate(), is(equalTo(end3)));
        assertThat("dragon was changed when changing endDate", lease1.getDragon(), is(equalTo(dragon3.getId())));
        assertThat("customer was changed when changing endDate", lease1.getCustomer(), is(equalTo(customer3.getId())));
        assertThat("startDate was changed when changing endDate", lease1.getStartDate(), is(equalTo(start3)));
        assertThat("price was changed when changing endDate", lease1.getPrice(), is(equalTo(200)));

        //change of price

        lease1.setPrice(5000);
        manager.updateLease(lease1);
        lease1 = manager.getLease(leaseId);


        assertThat("price was not changed", lease1.getPrice(), is(equalTo(5000)));
        assertThat("dragon was changed when changing countOfHeads", lease1.getDragon(), is(equalTo(dragon3.getId())));
        assertThat("customer was changed when changing countOfHeads", lease1.getCustomer(), is(equalTo(customer3.getId())));
        assertThat("startDate was changed when changing countOfHeads", lease1.getStartDate(), is(equalTo(start3)));
        assertThat("endDate was changed when changing countOfHeads", lease1.getEndDate(), is(equalTo(end3)));


        // Check if updates didn't affected other records
        assertDeepEquals(lease2, manager.getLease(lease2.getId()));
    }

    @Test
    public void updateLeaseWithWrongAttributes() {

        LocalDate start = LocalDate.of(2016,Month.FEBRUARY,2);
        LocalDate end = LocalDate.of(2016,Month.FEBRUARY,3);
        Customer customer = newCustomer("Ashen Shugar", "+999 666 333", "Iratus 7");
        Dragon dragon = newDragon("janca's dragon", 3, 100);

        dragonManager.createDragon(dragon);
        customerManager.createCustomer(customer);

        Lease lease = newLease(dragon,customer,start,end,200);
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
        dragonManager.createDragon(dragon1);
        dragonManager.createDragon(dragon2);
        customerManager.createCustomer(customer1);
        customerManager.createCustomer(customer2);

        LocalDate start = LocalDate.of(2016,Month.FEBRUARY,2);
        LocalDate end = LocalDate.of(2016,Month.FEBRUARY,3);
        Lease lease1 = newLease(dragon1,customer1,start,end,200);
        LocalDate start1 = LocalDate.of(2016,Month.MAY,5);
        LocalDate end1 = LocalDate.of(2016,Month.MAY,6);
        Lease lease2 = newLease(dragon2,customer2,start1,end1,200);

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
        LocalDate start =LocalDate.of(2016,Month.FEBRUARY,2);
        LocalDate end = LocalDate.of(2016,Month.FEBRUARY,3);
        Lease lease = newLease(dragon,customer,start,end,200);

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
        assertEquals("startDate value is not equal", expected.getStartDate(), actual.getStartDate());
        assertEquals("endDate value is not equal", expected.getEndDate(), actual.getEndDate());
    }

    private static Lease newLease(Dragon dragon, Customer customer, LocalDate startDate, LocalDate endDate, int price) {
        Lease lease = new Lease();

        lease.setDragon(dragon.getId());
        lease.setCustomer(customer.getId());
        lease.setStartDate(startDate);
        lease.setEndDate(endDate);
        lease.setPrice(price);
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