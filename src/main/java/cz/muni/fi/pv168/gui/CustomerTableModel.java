package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.Customer;
import cz.muni.fi.pv168.CustomerManager;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

public class CustomerTableModel extends AbstractTableModel {
    
    private static final Logger LOGGER = Logger.getLogger(CustomerTableModel.class.getName());
    private CustomerManager customerManager;
    private List<Customer> customers = new ArrayList<>();
    private static enum COLUMNS {
        ID, NAME, PHONE, ADDRESS
    }

    public void setCustomerManager(CustomerManager customerManager) {
        this.customerManager = customerManager;
    }
    
    @Override
    public int getRowCount() {
        return customers.size();
    }
 
    @Override
    public int getColumnCount() {
        return COLUMNS.values().length;
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
	switch (COLUMNS.values()[columnIndex]) {
	    case ID:
		return Integer.class;
	    case NAME:
                return String.class;
	    case PHONE:
                return String.class;
	    case ADDRESS:
		return String.class;
	    default:
		throw new IllegalArgumentException("columnIndex");
	}
    }
    
    @Override
    public String getColumnName(int columnIndex) { /** Predelat */
	switch (COLUMNS.values()[columnIndex]) {
	    case ID:
                return "ID";
		//return java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/autorental/gui/Bundle").getString("customers_table_id");
	    case NAME:
                return "NAME";
		//return java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/autorental/gui/Bundle").getString("customers_table_firstname");
	    case PHONE:
                return "PHONE";
		//return java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/autorental/gui/Bundle").getString("customers_table_lastname");
	    case ADDRESS:
                return "ADDRESS";
		//return java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/autorental/gui/Bundle").getString("customers_table_birth");
	    default:
		throw new IllegalArgumentException("columnIndex");
	}
    }
 
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Customer customer = customers.get(rowIndex);
        switch (COLUMNS.values()[columnIndex]) {
            case ID:
                return customer.getId();
            case NAME:
                return customer.getName();
            case PHONE:
                return customer.getPhone();
	    case ADDRESS:
                return customer.getAddress();         
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    
    public void addCustomer(Customer customer) {
	customers.add(customer);
	fireTableDataChanged();
    }
        
    public void removeCustomer(Customer customer) {
	customers.remove(customer);
	fireTableDataChanged();
    }
    
    public void clear() {
	customers.clear();
        fireTableDataChanged();
    }
    
     public List<Customer> getAllCustomers() {
	return customers;
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	Customer customer = customers.get(rowIndex);
	switch (columnIndex) {
	    case 0:
		customer.setId((Long) aValue);
		break;
	    case 1:
		customer.setName((String) aValue);
		break;
	    case 2:
		customer.setPhone((String) aValue);
		break;
	    case 3:
		customer.setAddress((String) aValue);
		break;
            
	    default:
		throw new IllegalArgumentException("columnIndex");
	}
        try {
            customerManager.updateCustomer(customer);
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
	    case NAME:
                return true;
	    case PHONE:
                return true;
	    case ADDRESS:
		return true;
	    default:
		throw new IllegalArgumentException("columnIndex");
	}
    }

}
