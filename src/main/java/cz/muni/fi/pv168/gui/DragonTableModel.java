package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.Dragon;
import cz.muni.fi.pv168.DragonManager;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

public class DragonTableModel extends AbstractTableModel {
    
    private static final Logger LOGGER = Logger.getLogger(DragonTableModel.class.getName());
    private DragonManager dragonManager;
    private List<Dragon> dragons = new ArrayList<>();
    private static enum COLUMNS {
        ID, NAME, HEADS, PRICE
    }
    
    public void setDragonManager(DragonManager dragonManager) {
        this.dragonManager = dragonManager;
    }
 
    @Override
    public int getRowCount() {
        return dragons.size();
    }
 
    @Override
    public int getColumnCount() {
        return COLUMNS.values().length;
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
	switch (columnIndex) {
	    case 0:
		return Long.class;
	    case 1:
                return String.class;
	    case 2:
		return Integer.class;
	    case 3:
		return Integer.class;
	    default:
		throw new IllegalArgumentException("columnIndex");
	}
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Dragon dragon = dragons.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return dragon.getId();
            case 1:
                return dragon.getName();
            case 2:
                return dragon.getCountOfHeads();
            case 3:
                return dragon.getPriceForDay();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    public void addDragon(Dragon dragon) {
	dragons.add(dragon);
	fireTableDataChanged();
    }
    
    public void removeDragon(Dragon dragon) {
        dragons.remove(dragon);
	fireTableDataChanged();
    }
    
    public void clear() {
	dragons.clear();
        fireTableDataChanged();
    }
    
     public List<Dragon> getAllDragons() {
	return dragons;
    }

    
    @Override
    public String getColumnName(int columnIndex) { /** predelat!*/
	switch (COLUMNS.values()[columnIndex]) {
	    case ID:
                return "ID";
		//return java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/autorental/gui/Bundle").getString("cars_table_id");
	    case NAME:
                return "NAME";
		//return java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/autorental/gui/Bundle").getString("cars_table_model");
	    case HEADS:
                return "HEADS";
		//return java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/autorental/gui/Bundle").getString("cars_table_plate");
	    case PRICE:
                return "PRICE";
		//return java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/autorental/gui/Bundle").getString("cars_table_fee");
	    default:
		throw new IllegalArgumentException("columnIndex");
	}
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	Dragon dragon = dragons.get(rowIndex);
	switch (COLUMNS.values()[columnIndex]) {
	    case NAME:
		dragon.setName((String) aValue);
		break;
	    case HEADS:
		dragon.setCountOfHeads((Integer) aValue);
		break;
	    case PRICE:
		dragon.setPriceForDay((Integer) aValue);
		break;
	    default:
		throw new IllegalArgumentException("columnIndex");
	}
        try {
            dragonManager.updateDragon(dragon);
            fireTableDataChanged();
        } catch (Exception ex) {
            String msg = "User request failed";
            LOGGER.log(Level.INFO, msg);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	switch (columnIndex) {
	    case 1:
                return true;
	    case 2:
                return true;
            case 3:
		return true;
	    case 0:
		return false;
	    default:
		throw new IllegalArgumentException("columnIndex");
	}
    }

}
