package cz.muni.fi.pv168;

import cz.muni.fi.pv168.common.DBUtils;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;



import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import javax.sql.DataSource;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;



/**
 * @author: Jana Zahradnickova,  UCO 433598
 * @version: 8. 3. 2016
 */
public class DragonManagerTest {
    private static final Logger LOGGER = Logger.getLogger(DragonManagerImpl.class.getName());
    private DragonManagerImpl manager;
    private DataSource dataSource;

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        DBUtils.executeSqlScript(dataSource, DragonManager.class.getResource("db_create.sql"));
        manager = new DragonManagerImpl(dataSource);
    }

    private DataSource prepareDataSource() {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        //we will use in memory database
        ds.setDatabaseName("memory:dragonmgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(dataSource, DragonManager.class.getResource("db_drop.sql"));
    }

    @Test
    public void createDragon() {
        Dragon dragon = newDragon("janca's dragon", 3, 100);
        manager.createDragon(dragon);

        Long dragonId = dragon.getId();
        assertThat("saved dragon has null id", dragon.getId(), is(not(equalTo(null))));


        Dragon result = manager.getDragon(dragonId);
        //loaded instance should be equal to the saved one
        assertThat("loaded dragon different from the saved one", result, is(equalTo(dragon)));
        //but it should be another instance
        assertThat("loaded dragon is the same instance", result, is(not(sameInstance(dragon))));
        //equals method test
        assertDeepEquals(dragon, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDragonWithNull() throws Exception {
        manager.createDragon(null);
    }

    @Test
    public void createDragonWithWrongValues() {
        Dragon dragon = newDragon("janca's dragon", 3, 100);
        dragon.setId(1L);
        try {
            manager.createDragon(dragon);
            fail("should refuse assigned id");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        dragon = newDragon("janca's dragon", -1, 100);
        try {
            manager.createDragon(dragon);
            fail("negative countOfHeads number not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        dragon = newDragon("janca's dragon", 3, -1);
        try {
            manager.createDragon(dragon);
            fail("negative priceForDay not detected");
        } catch (IllegalArgumentException ex) {
            //OK
        }
    }

    @Test
    public void getAllDragons() {

        assertTrue(manager.findAllDragon().isEmpty());

        Dragon d1 = newDragon("janca's first dragon", 3, 100);
        Dragon d2 = newDragon("janca's second dragon", 5, 200);

        manager.createDragon(d1);
        manager.createDragon(d2);

        List<Dragon> expected = Arrays.asList(d1, d2);
        List<Dragon> actual = manager.findAllDragon();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals("saved and retrieved dragons differ", expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void updateDagon() {
        Dragon dragon = newDragon("janca's dragon", 3, 100);
        Dragon secondDragon = newDragon("janca's second dragon", 5, 200);
        manager.createDragon(dragon);
        manager.createDragon(secondDragon);
        Long dragonId = dragon.getId();

        //change of countOfHeads
        dragon.setCountOfHeads(1); //zbyle dve byly useknuty :'(
        manager.updateDragon(dragon);
        //load from database
        dragon = manager.getDragon(dragonId);

        assertThat("countOfHeads was not changed", dragon.getCountOfHeads(), is(equalTo(1)));
        assertThat("priceForDay was changed when changing countOfHeads", dragon.getPriceForDay(), is(equalTo(100)));
        assertThat("name was changed when changing countOfHeads", dragon.getName(), is(equalTo("janca's dragon")));

        //change of priceForDay
        dragon.setPriceForDay(200);
        manager.updateDragon(dragon);
        //load from database
        dragon = manager.getDragon(dragonId);

        assertThat("countOfHeads was changed when changing PriceForDay", dragon.getCountOfHeads(), is(equalTo(1)));
        assertThat("priceForDay was not changed", dragon.getPriceForDay(), is(equalTo(200)));
        assertThat("name was changed when changing PriceForDay", dragon.getName(), is(equalTo("janca's dragon")));

        //change of name
        dragon.setName("tom's dragon");
        manager.updateDragon(dragon);
        dragon = manager.getDragon(dragonId);

        assertThat("countOfHeads was changed when changing name", dragon.getCountOfHeads(), is(equalTo(1)));
        assertThat("priceForDay was changed when changing name", dragon.getPriceForDay(), is(equalTo(200)));
        assertThat("name as not changed", dragon.getName(), is(equalTo("tom's dragon")));

        dragon.setName(null);
        manager.updateDragon(dragon);
        dragon = manager.getDragon(dragonId);
        assertEquals(1, dragon.getCountOfHeads());
        assertEquals(200, dragon.getPriceForDay());
        assertNull(dragon.getName());

        // Check if updates didn't affected other records
        assertDeepEquals(secondDragon, manager.getDragon(secondDragon.getId()));
    }

    @Test
    public void updateDragonWithWrongAttributes() {

        Dragon dragon = newDragon("janca's dragon", 3, 100);
        manager.createDragon(dragon);
        Long dragonId = dragon.getId();

        try {
            manager.updateDragon(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            dragon = manager.getDragon(dragonId);
            long id = Long.parseLong(null);
            dragon.setId(id);
            manager.updateDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            dragon = manager.getDragon(dragonId);
            dragon.setId(dragonId - 1);
            manager.updateDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            dragon = manager.getDragon(dragonId);
            dragon.setCountOfHeads(-1);
            manager.updateDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            dragon = manager.getDragon(dragonId);
            dragon.setPriceForDay(-1);
            manager.updateDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
    }

    @Test
    public void deleteDragon() {

        Dragon dragon1 = newDragon("janca's dragon", 3, 100);
        Dragon dragon2 = newDragon("janca's second dragon", 5, 200);
        manager.createDragon(dragon1);
        manager.createDragon(dragon2);

        assertNotNull(manager.getDragon(dragon1.getId()));
        assertNotNull(manager.getDragon(dragon2.getId()));

        manager.deleteDragon(dragon1);

        assertNull(manager.getDragon(dragon1.getId()));
        assertNotNull(manager.getDragon(dragon2.getId()));
    }

    @Test
    public void deleteDragonWithWrongAttributes() {

        Dragon dragon = newDragon("janca's dragon", 3, 100);

        try {
            manager.deleteDragon(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            long id = Long.parseLong(null);
            dragon.setId(id);
            manager.deleteDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            dragon.setId(1L);
            manager.deleteDragon(dragon);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
    }

    private void assertDeepEquals(List<Dragon> expectedList, List<Dragon> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Dragon expected = expectedList.get(i);
            Dragon actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(Dragon expected, Dragon actual) {
        assertEquals("id value is not equal", expected.getId(), actual.getId());
        assertEquals("name value is not equal", expected.getName(), actual.getName());
        assertEquals("countOfHeads value is not equal", expected.getCountOfHeads(), actual.getCountOfHeads());
        assertEquals("priceForDay value is not equal", expected.getPriceForDay(), actual.getPriceForDay());
    }

    private static Dragon newDragon(String name, int countOfHeads, int priceForDay) {
        Dragon dragon = new Dragon();

        //   dragon.setId();
        dragon.setName(name);
        dragon.setCountOfHeads(countOfHeads);
        dragon.setPriceForDay(priceForDay);
        return dragon;
    }

    private static Comparator<Dragon> idComparator = new Comparator<Dragon>() {
        @Override
            public int compare(Dragon o1, Dragon o2) {
                return o1.getId().compareTo(o2.getId());
        }

    };
}
