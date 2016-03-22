package cz.muni.fi.pv168;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jana Zahradnickova,  UCO 433598
 * @version: 8. 3. 2016
 */
public class DragonManagerImpl implements DragonManager {
    final static Logger log = LoggerFactory.getLogger(DragonManagerImpl.class);

    private final DataSource dataSource;

    public DragonManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createDragon(Dragon dragon) throws ServiceFailureException {
        if (dragon == null) {
            throw new IllegalArgumentException("dragon is null");
        }
        if (dragon.getId() != null) {
            throw new IllegalArgumentException("dragon id is already set");
        }
        if (dragon.getCountOfHeads() < 1) {
            throw new IllegalArgumentException("dragon countOfHeads < 1 ");
        }
        if (dragon.getPriceForDay() < 0) {
            throw new IllegalArgumentException("dragon priceForDay is negative number");
        }

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO dragon (name,countOfHeads,priceGorDay) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                st.setString(1, dragon.getName());
                st.setInt(2, dragon.getCountOfHeads());
                st.setInt(3, dragon.getPriceForDay());
                int addedRows = st.executeUpdate();
                if (addedRows != 1) {
                    throw new ServiceFailureException("Internal Error: More rows inserted when trying to insert dragon " + dragon);
                }
                ResultSet keyRS = st.getGeneratedKeys();
                dragon.setId(getKey(keyRS, dragon));
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all dragons", ex);
        }

    }

    private Long getKey(ResultSet keyRS, Dragon dragon) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert dragon " + dragon
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert dragon " + dragon
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retrieving failed when trying to insert dragon " + dragon
                    + " - no key found");
        }
    }

    @Override
    public Dragon getDragon(Long id) throws ServiceFailureException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,name,countOfHeads,priceForDay FROM dragon WHERE id = ?")) {
                st.setLong(1, id);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    Dragon dragon = resultSetToDragon(rs);
                    if (rs.next()) {
                        throw new ServiceFailureException(
                                "Internal error: More entities with the same id found "
                                        + "(source id: " + id + ", found " + dragon + " and " + resultSetToDragon(rs));
                    }
                    return dragon;
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all dragons", ex);
        }
    }

    private Dragon resultSetToDragon(ResultSet rs) throws SQLException {
        Dragon dragon = new Dragon();
        dragon.setId(rs.getLong("id"));
        dragon.setName(rs.getString("name"));
        dragon.setCountOfHeads(rs.getInt("countOfHeads"));
        dragon.setPriceForDay(rs.getInt("priceForDay"));
        return dragon;
    }

    @Override
    public void updateDragon(Dragon dragon) throws ServiceFailureException {
        if (dragon == null) throw new IllegalArgumentException("dragon pointer is null");
        if (dragon.getId() == null) throw new IllegalArgumentException("dragon with null id cannot be updated");
        if (dragon.getCountOfHeads() < 1)
            throw new IllegalArgumentException("dragon countOfHeads is not positive number");
        if (dragon.getPriceForDay() < 0) throw new IllegalArgumentException("dragon priceForDay is negative number");

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("UPDATE dragon SET name=?,countOfHeads=?,priceForDay=? WHERE id=?")) {
                st.setString(1, dragon.getName());
                st.setInt(2, dragon.getCountOfHeads());
                st.setInt(3, dragon.getPriceForDay());
                st.setLong(4, dragon.getId());
                if (st.executeUpdate() != 1) {
                    throw new IllegalArgumentException("cannot update dragon " + dragon);
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all dragons", ex);
        }
    }

    @Override
    public void deleteDragon(Dragon dragon) throws ServiceFailureException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM dragon WHERE id=?")) {
                st.setLong(1, dragon.getId());
                if (st.executeUpdate() != 1) {
                    throw new ServiceFailureException("did not delete dragon with id =" + dragon);
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all dragons", ex);
        }
    }

    @Override
    public List<Dragon> findAllDragon() throws ServiceFailureException {
        log.debug("finding all dragons");
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,name,countOfHeads,priceForDay FROM dragon")) {
                ResultSet rs = st.executeQuery();
                List<Dragon> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToDragon(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new ServiceFailureException("Error when retrieving all dragons", ex);
        }
    }
}

