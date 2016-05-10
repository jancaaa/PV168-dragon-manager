package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.Lease;
import cz.muni.fi.pv168.LeaseManager;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

public class LeaseTableModel extends AbstractTableModel {
 
    private static final Logger LOGGER = Logger.getLogger(LeaseTableModel.class.getName());
    private LeaseManager leaseManager;
    private List<Lease> leases = new ArrayList<>();
    private static enum COLUMNS {
        ID,  CUSTOMER, DRAGON, FROM, TO, COST
    }

    public void setLeaseManager(LeaseManager leaseManager) {
        this.leaseManager = leaseManager;
    }
    
 
    @Override
    public int getRowCount() {
        return leases.size();
    }
 
    @Override
    public int getColumnCount() {
        return COLUMNS.values().length;
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
	switch (COLUMNS.values()[columnIndex]) {
	    case ID:
		return Long.class;
	    case CUSTOMER:
                return String.class;
	    case DRAGON:
		return String.class;
	    case FROM:
                return Date.class;
	    case TO:
		return Date.class;
	    case COST:
		return Integer.class;
	    default:
		throw new IllegalArgumentException("columnIndex");
	}
    }
    
    @Override
    public String getColumnName(int columnIndex) { /** Predelat */
	switch (COLUMNS.values()[columnIndex]) {
	    case ID:
                return "ID";
		//return java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/autorental/gui/Bundle").getString("rents_table_id");
	    case CUSTOMER:
                return "CUSTOMER";
		//return java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/autorental/gui/Bundle").getString("rents_table_customer");
	    case DRAGON:
                return "DRAGON";
		//return java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/autorental/gui/Bundle").getString("rents_table_car");
	    case FROM:
                return "FROM";
		//return java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/autorental/gui/Bundle").getString("rents_table_from");
	    case TO:
                return "TO";
		//return java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/autorental/gui/Bundle").getString("rents_table_to");
	    case COST:
                return "COST";
                //return java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/autorental/gui/Bundle").getString("rents_table_cost");
	    default:
		throw new IllegalArgumentException("columnIndex");
	}
    }
 
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Lease lease = leases.get(rowIndex);
        switch (COLUMNS.values()[columnIndex]) {
            case ID:
                return lease.getId();
            case CUSTOMER:
                return lease.getCustomer();
            case DRAGON:
                return lease.getDragon();
	    case FROM:
                return lease.getStartDate();
            case TO:
                return lease.getEndDate();
	    case COST:
                return lease.getPrice();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    public void addLease(Lease lease) {
	leases.add(lease);
	fireTableDataChanged();
    }
    
    public void removeLease(Lease lease) {
	leases.remove(lease);
	fireTableDataChanged();
    }
    
    public void clear() {
	leases.clear();
        fireTableDataChanged();
    }
    
     public List<Lease> getAllLeases() {
	return leases;
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	Lease lease = leases.get(rowIndex);
	switch (COLUMNS.values()[columnIndex]) {
	    case FROM:
		lease.setStartDate((LocalDate) aValue);
		break;
	    case TO:
		lease.setEndDate((LocalDate) aValue);
		break;
	    case COST:
		lease.setPrice(new Integer((String) aValue));
		break;
	    default:
		throw new IllegalArgumentException("columnIndex");
	}
        try {
            leaseManager.updateLease(lease);
            fireTableDataChanged();
        } catch (Exception ex) {
            String msg = "User request failed";
            LOGGER.log(Level.INFO, msg);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	switch (COLUMNS.values()[columnIndex]) {
	    case ID:
                return false;
	    case CUSTOMER:       
                return false;
	    case DRAGON:
		return false;
	    case FROM:
                return true;
	    case TO:
                return true;
	    case COST:
		return true;
	    default:
		throw new IllegalArgumentException("columnIndex");
	}
    }

}
