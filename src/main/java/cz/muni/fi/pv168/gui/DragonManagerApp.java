package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.gui.*;
import cz.muni.fi.pv168.*;
import cz.muni.fi.pv168.gui.DragonManagerLay;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.swing.table.TableColumn;

import javax.sql.DataSource;


/**
 *
 * @author Janca
 */
public class DragonManagerApp extends javax.swing.JFrame {
    DataSource dataSource = new DataSource();
    private static final Logger LOGGER = Logger.getLogger(DragonManagerApp.class.getName());
    private String action;
    
    LeaseManager leaseManager;
    CustomerManager customerManager;
    DragonManager dragonManager;
    
    LeaseTableModel leaseTableModel;
    CustomerTableModel customerTableModel;
    DragonTableModel dragonTableModel;
    
    private LeaseSwingWorker leaseSwingWorker;
    
    
    private class LeaseSwingWorker extends SwingWorker<Void, Lease> {

	@Override
	protected Void doInBackground() throws Exception {
	    leaseTableModel = (LeaseTableModel) leases_table.getModel();
            leaseTableModel.setLeaseManager(leaseManager);
            int counter = 0;
	    for (Lease lease : leaseManager.findAllLeases()) {
                counter++;
                Thread.sleep(150);
		publish(lease);
                setProgress(counter);
	    }
	    return null;
	}
	
	@Override
	protected void process(List<Lease> items) {
	    for (Lease i : items) {
                leaseTableModel.addLease(i);
	    }
	}

        @Override
        protected void done() {
            //leases_load.setEnabled(true);
           //leases_progress.setValue(100);
            leaseSwingWorker = null;
        }
    }
    private CustomersSwingWorker customersSwingWorker;
    private class CustomersSwingWorker extends SwingWorker<Void, Customer> {

	@Override
	protected Void doInBackground() throws Exception {
	//    customerTableName = (CustomerTableName) customers_table.getName();
            customerTableModel.setCustomerManager(customerManager);
            int counter = 0;
	    for (Customer customer : customerManager.findAllCustomers()) {
                counter++;
                Thread.sleep(50);
		publish(customer);
                setProgress(counter);
	    }
	    return null;
	}
	
	@Override
	protected void process(List<Customer> items) {
	    for (Customer i : items) {
                customerTableModel.addCustomer(i);
	    }
	}

        @Override
        protected void done() {
          //  customers_load.setEnabled(true);
          //  customers_progress.setValue(100);
            customersSwingWorker = null;
        }
    }
    private DragonsSwingWorker dragonsSwingWorker;
    private class DragonsSwingWorker extends SwingWorker<Void, Dragon> {

	@Override
	protected Void doInBackground() throws Exception {
	   // dragonTableModel = (DragonTableModel) dragons_table.getModel();
           // dragonTableModel.setdragonManager(dragonManager);
            int counter = 0;
	    for (Dragon dragon : dragonManager.findAllDragon()) {
                counter++;
                Thread.sleep(100);
		publish(dragon);
                setProgress(counter);
	    }
	    return null;
	}
	
	@Override
	protected void process(List<Dragon> items) {
	    for (Dragon i : items) {
                dragonTableModel.addAdragon(i);
	    }
	}

        @Override
        protected void done() {
           // dragons_load.setEnabled(true);
           // dragons_progress.setValue(100);
            dragonsSwingWorker = null;
        }
    }
    
    private void setUp() throws Exception {
        Properties configFile = new Properties();
        configFile.load(new FileInputStream("src/config.properties"));
	//DataSource ds = new DataSource();
	//ds.setUrl( configFile.getProperty( "url" ) );
	//ds.setPassword( configFile.getProperty( "password" ) );
	//ds.setUsername( configFile.getProperty( "username" ) );
	//DataSource = ds;
    }
    
    

    /**
     * Creates new form DragonManager
     */
    public DragonManagerApp() {
        initComponents();
        
        leaseManager	= new LeaseManagerImpl(DataSource);
        customerManager = new CustomerManagerImpl(DataSource);
        dragonManager	= new DragonManagerImpl(DataSource);
        
        leasesSwingWorker = new DragonManagerApp.leasesSwingWorker();
        leasesSwingWorker.addPropertyChangeListener(leasesProgressListener);
        leasesSwingWorker.execute();
        
        customersSwingWorker = new DragonManagerApp.CustomersSwingWorker();
        customersSwingWorker.addPropertyChangeListener(customersProgressListener);
        customersSwingWorker.execute();
        
        dragonsSwingWorker = new DragonManagerApp.dragonsSwingWorker();
        dragonsSwingWorker.addPropertyChangeListener(dragonsProgressListener);
        dragonsSwingWorker.execute();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
        @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void dialog_dragons_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialog_dragons_cancelActionPerformed
	dialog_dragons.setVisible(false);
    }//GEN-LAST:event_dialog_dragons_cancelActionPerformed

    private void dialog_dragons_submitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialog_dragons_submitActionPerformed
        Dragon dragon = new Dragon();
        /* Name */
	dragon.setName(dialog_dragons_nameInput.getText());
        /* Plate */
	dragon.setCountOFHeads(dialog_dragons_headsInput.getText());
        /* Fee */
        try {
            dragon.setFee(BigDecimal.valueOf(Double.parseDouble(dialog_dragons_feeInput.getText())).setScale(2));
        } catch(NumberFormatException e) {
            String msg = "dragon fee wrong format";
            LOGGER.log(Level.INFO, msg);
        }
        
        try {
            /* dragon ID */
            if (dialog_dragons_idInput.getText().equals("")) { // Add
                LOGGER.log(Level.INFO, "Adding dragon");
                dragonManager.adddragon(dragon);
                dragonTableName.addDragon(dragon);
            } else { // Update
                LOGGER.log(Level.INFO, "Updating dragon");
                Long dragonId = Long.valueOf(dialog_dragons_idInput.getText());
                dragon.setId(dragonId);
                dragon dragonCached = dragonManager.finddragonById(dragonId);
                dragonManager.updatedragon(dragon);
                dragonTableName.removeDragon(dragonCached);
                dragonTableName.addDragon(dragon);
            }
            dialog_dragons.setVisible(false);
        } catch (Exception ex) {
            String msg = "User request failed";
            LOGGER.log(Level.INFO, msg);
        }
    }//GEN-LAST:event_dialog_dragons_submitActionPerformed

    private void leases_addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leases_addActionPerformed
        dialog_leases_idInput.setText("");
        dialog_leases_customerInput.setText("");
        dialog_leases_customerHelper.setText(java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/DragonManagerApp/gui/Bundle").getString("DragonManagerApp.dialog_leases_customerHelper.text"));
        dialog_leases_dragonInput.setText("");
        dialog_leases_dragonHelper.setText(java.util.ResourceBundle.getBundle("cz/muni/fi/pv168/DragonManagerApp/gui/Bundle").getString("DragonManagerApp.dialog_leases_dragonHelper.text"));
        dialog_leases_fromInput.setText("");
        dialog_leases_toInput.setText("");
        dialog_leases_costInput.setText("");
        dialog_leases.pack();
        dialog_leases.setLocationRelativeTo(null);
        dialog_leases.setVisible(true);
    }//GEN-LAST:event_leases_addActionPerformed

    private void customers_addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customers_addActionPerformed
        dialog_customers_idInput.setText("");
        dialog_customers_firstnameInput.setText("");
        dialog_customers_lastnameInput.setText("");
        dialog_customers_birthInput.setText("");
        dialog_customers_emailInput.setText("");
        dialog_customers.pack();
        dialog_customers.setLocationRelativeTo(null);
        dialog_customers.setVisible(true);
    }//GEN-LAST:event_customers_addActionPerformed

    private void dragons_addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dragons_addActionPerformed
        dialog_dragons_idInput.setText("");
        dialog_dragons_nameInput.setText("");
        dialog_dragons_headsInput.setText("");
        dialog_dragons_feeInput.setText("");
        dialog_dragons.pack();
        dialog_dragons.setLocationRelativeTo(null);
        dialog_dragons.setVisible(true);
    }//GEN-LAST:event_dragons_addActionPerformed

    private void dragons_updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dragons_updateActionPerformed
	action = "update";
        
        Long dragon_id = null;
        try {
            dragon_id = (Long) dragonTableName.getValueAt(dragons_table.getSelectedRow(), 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            return;
        }
        
        dragon dragon = dragonManager.finddragonById(dragon_id);
        
        dialog_dragons_idInput.setText(String.valueOf(dragon.getId()));
        dialog_dragons_nameInput.setText(dragon.getName());
        dialog_dragons_headsInput.setText(dragon.getPlate());
        dialog_dragons_feeInput.setText(String.valueOf(dragon.getFee()));
        
        dialog_dragons.pack();
        dialog_dragons.setLocationRelativeTo(null);
        dialog_dragons.setVisible(true);
    }//GEN-LAST:event_dragons_updateActionPerformed

    private void dialog_dragons_idInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialog_dragons_idInputActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_dialog_dragons_idInputActionPerformed

    private void dialog_customers_idInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialog_customers_idInputActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_dialog_customers_idInputActionPerformed

    private void dialog_customers_firstnameInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialog_customers_firstnameInputActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_dialog_customers_firstnameInputActionPerformed

    private void dialog_customers_submitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialog_customers_submitActionPerformed
        Customer customer = new Customer();
        /* Firstname */
	customer.setFirstname(dialog_customers_firstnameInput.getText());
        /* Lastname */
	customer.setLastname(dialog_customers_lastnameInput.getText());
        /* E-mail */
	customer.setEmail(dialog_customers_emailInput.getText());
        /* Birth */
        try {
            customer.setBirth(Date.valueOf(dialog_customers_birthInput.getText()));
        } catch(IllegalArgumentException e) {
            String msg = "Customer birth wrong format";
            LOGGER.log(Level.SEVERE, msg);
        }
        try {
            /* Customer ID */
            if (dialog_customers_idInput.getText().equals("")) { // Add
                LOGGER.log(Level.INFO, "Adding customer");
                customerManager.addCustomer(customer);
                customerTableModel.addCustomer(customer);
            } else { // Update
                LOGGER.log(Level.INFO, "Updating customer");
                Long customerId = Long.valueOf(dialog_customers_idInput.getText());
                customer.setId(customerId);
                Customer customerCached = customerManager.findCustomerById(customerId);
                customerManager.updateCustomer(customer);
                customerTableModel.removeCustomer(customerCached);
                customerTableModel.addCustomer(customer);
            }
            dialog_customers.setVisible(false);
        } catch (Exception ex) {
            String msg = "User request failed";
            LOGGER.log(Level.INFO, msg);
        }
    }//GEN-LAST:event_dialog_customers_submitActionPerformed

    private void dialog_customers_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialog_customers_cancelActionPerformed
	dialog_customers.setVisible(false);
    }//GEN-LAST:event_dialog_customers_cancelActionPerformed

    private void dialog_leases_idInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialog_leases_idInputActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_dialog_leases_idInputActionPerformed

    private void dialog_leases_submitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialog_leases_submitActionPerformed
	lease lease = new lease();
        lease.setCustomer(customerManager.findCustomerById(Long.parseLong(dialog_leases_customerInput.getText())));
        lease.setdragon(dragonManager.finddragonById(Long.parseLong(dialog_leases_dragonInput.getText())));

        /* From, To */
        try {
            lease.setFrom(Date.valueOf(dialog_leases_fromInput.getText()));
            lease.setTo(Date.valueOf(dialog_leases_toInput.getText()));
        } catch (IllegalArgumentException ex) {
            String msg = "lease from or to wrong format";
            LOGGER.log(Level.SEVERE, msg);
        }
        /* Cost */
        try {
            lease.setCost(BigDecimal.valueOf(Double.parseDouble(dialog_leases_costInput.getText())).setScale(2));
        } catch (NumberFormatException ex) { // chyba prevodu String -> Double
            String msg = "lease cost wrong format";
            LOGGER.log(Level.SEVERE, msg);
        }
        
        try {
            /* lease ID */
            if (dialog_leases_idInput.getText().equals("")) { // Add
                LOGGER.log(Level.INFO, "Adding lease");
                leaseManager.addlease(lease);
                leaseTableModel.addlease(lease);
            } else { // Update
                LOGGER.log(Level.INFO, "Updating lease");
                Long leaseId = Long.valueOf(dialog_leases_idInput.getText());
                lease.setId(leaseId);
                lease leaseCached = leaseManager.findleaseById(leaseId);
                leaseManager.updatelease(lease);
                leaseTableModel.removelease(leaseCached);
                leaseTableModel.addlease(lease);
            }
            dialog_leases.setVisible(false);
        } catch (Exception ex) {
            String msg = "User request failed";
            LOGGER.log(Level.INFO, msg);
        }
    }//GEN-LAST:event_dialog_leases_submitActionPerformed

    private void dialog_leases_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialog_leases_cancelActionPerformed
	dialog_leases.setVisible(false);
    }//GEN-LAST:event_dialog_leases_cancelActionPerformed

    private void dialog_leases_calculateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialog_leases_calculateActionPerformed
	lease lease = new lease();
        lease.setdragon(dragonManager.finddragonById(Long.parseLong(dialog_leases_dragonInput.getText())));

        /* From, To */
        try {
            lease.setFrom(Date.valueOf(dialog_leases_fromInput.getText()));
            lease.setTo(Date.valueOf(dialog_leases_toInput.getText()));
        } catch (IllegalArgumentException ex) {
            String msg = "lease from or to wrong format";
            LOGGER.log(Level.SEVERE, msg);
        }
        /* Calculate and set cost input */
        dialog_leases_costInput.setText(lease.calculateCost().toString());
        
    }//GEN-LAST:event_dialog_leases_calculateActionPerformed

    private void dialog_customers_emailInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialog_customers_emailInputActionPerformed
	// TODO add your handling code here:
    }//GEN-LAST:event_dialog_customers_emailInputActionPerformed

    private void leases_updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leases_updateActionPerformed
        Long lease_id = (Long) leaseTableModel.getValueAt(leases_table.getSelectedRow(), 0);
        lease lease = leaseManager.findleaseById(lease_id);
        dialog_leases_idInput.setText(String.valueOf(lease.getId()));
        
        dialog_leases_customerInput.setText(String.valueOf(lease.getCustomer().getId()));
        dialog_leases_customerHelper.setText(String.valueOf(lease.getCustomer().toString()));
        
        dialog_leases_dragonInput.setText(String.valueOf(lease.getdragon().getId()));
        dialog_leases_dragonHelper.setText(String.valueOf(lease.getdragon().toString()));
        
        dialog_leases_fromInput.setText(String.valueOf(leaseTableModel.getValueAt(leases_table.getSelectedRow(), 3)));
        dialog_leases_toInput.setText(String.valueOf(leaseTableModel.getValueAt(leases_table.getSelectedRow(), 4)));
        dialog_leases_costInput.setText(String.valueOf(leaseTableModel.getValueAt(leases_table.getSelectedRow(), 5)));
        
        dialog_leases.pack();
        dialog_leases.setLocationRelativeTo(null);
        dialog_leases.setVisible(true);
    }//GEN-LAST:event_leases_updateActionPerformed

    private void customers_useActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customers_useActionPerformed
        Long id = (Long) customerTableModel.getValueAt(customers_table.getSelectedRow(), 0);
        Customer c = customerManager.findCustomerById(id); // TODO: Use TableName instead of manager
        dialog_leases_customerInput.setText(String.valueOf(id));
        dialog_leases_customerHelper.setText(String.valueOf(c.toString()));
    }//GEN-LAST:event_customers_useActionPerformed

    private void dragons_useActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dragons_useActionPerformed
        Long id = (Long) dragonTableName.getValueAt(dragons_table.getSelectedRow(), 0);
        dragon c = dragonManager.finddragonById(id); // TODO: Use TableName instead of manager
        dialog_leases_dragonInput.setText(String.valueOf(c.getId()));
        dialog_leases_dragonHelper.setText(String.valueOf(c.toString()));
    }//GEN-LAST:event_dragons_useActionPerformed

    private void leases_loadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leases_loadActionPerformed
        if (leasesSwingWorker != null) {
            throw new IllegalStateException("Operation is already in progress");
        }
        leases_load.setEnabled(false);
        leases_progress.setValue(0);
        leaseTableModel.clear();
        leasesSwingWorker = new leasesSwingWorker();
        leasesSwingWorker.addPropertyChangeListener(leasesProgressListener);
        leasesSwingWorker.execute();
    }//GEN-LAST:event_leases_loadActionPerformed

    private void customers_loadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customers_loadActionPerformed
        if (customersSwingWorker != null) {
            throw new IllegalStateException("Operation is already in progress");
        }
        customers_load.setEnabled(false);
        customers_progress.setValue(0);
        customerTableModel.clear();
        customersSwingWorker = new CustomersSwingWorker();
        customersSwingWorker.addPropertyChangeListener(customersProgressListener);
        customersSwingWorker.execute();
    }//GEN-LAST:event_customers_loadActionPerformed

    private void dragons_loadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dragons_loadActionPerformed
        if (dragonsSwingWorker != null) {
            throw new IllegalStateException("Operation is already in progress");
        }
        dragons_load.setEnabled(false);
        dragons_progress.setValue(0);
        dragonTableName.clear();
        dragonsSwingWorker = new dragonsSwingWorker();
        dragonsSwingWorker.addPropertyChangeListener(dragonsProgressListener);
        dragonsSwingWorker.execute();
    }//GEN-LAST:event_dragons_loadActionPerformed

    private void dragons_deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dragons_deleteActionPerformed
        Long dragon_id = null;
        try {
            dragon_id = (Long) dragonTableName.getValueAt(dragons_table.getSelectedRow(), 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            String msg = "No row selected";
            LOGGER.log(Level.INFO, msg);
        }
        
        dragon dragon = dragonManager.finddragonById(dragon_id);
        try {
            for (lease lease : leaseManager.findAlldragonleases(dragon)) {
                leaseManager.removelease(lease);
                leaseTableModel.removelease(lease);
	    }
            dragonManager.removedragon(dragon);
            dragonTableName.removeDragon(dragon);
        } catch (Exception ex) {
            String msg = "Deleting failed";
            LOGGER.log(Level.INFO, msg);
        }
    }//GEN-LAST:event_dragons_deleteActionPerformed

    private void customers_updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customers_updateActionPerformed
        Long customer_id = (Long) customerTableModel.getValueAt(customers_table.getSelectedRow(), 0);
        Customer customer = customerManager.findCustomerById(customer_id);
        dialog_customers_idInput.setText(String.valueOf(customer.getId()));
        
        dialog_customers_firstnameInput.setText(String.valueOf(customer.getFirstname()));
        dialog_customers_lastnameInput.setText(String.valueOf(customer.getLastname()));
        dialog_customers_birthInput.setText(String.valueOf(customer.getBirth()));
        dialog_customers_emailInput.setText(String.valueOf(customer.getEmail()));
        
        dialog_customers.pack();
        dialog_customers.setLocationRelativeTo(null);
        dialog_customers.setVisible(true);
    }//GEN-LAST:event_customers_updateActionPerformed

    private void customers_deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customers_deleteActionPerformed
        Long customer_id = null;
        try {
            customer_id = (Long) customerTableModel.getValueAt(customers_table.getSelectedRow(), 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            String msg = "No row selected";
            LOGGER.log(Level.INFO, msg);
        }
        
        Customer customer = customerManager.findCustomerById(customer_id);
        try {
            for (lease lease : leaseManager.findAllCustomerleases(customer)) {
                leaseManager.removelease(lease);
                leaseTableModel.removelease(lease);
	    }
            customerManager.removeCustomer(customer);
            customerTableModel.removeCustomer(customer);
        } catch (Exception ex) {
            String msg = "Deleting failed";
            LOGGER.log(Level.INFO, msg);
        }
    }//GEN-LAST:event_customers_deleteActionPerformed

    private void leases_deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leases_deleteActionPerformed
        Long lease_id = null;
        try {
            lease_id = (Long) leaseTableModel.getValueAt(leases_table.getSelectedRow(), 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            String msg = "No row selected";
            LOGGER.log(Level.INFO, msg);
        }
        
        lease lease = leaseManager.findleaseById(lease_id);
        try {
            leaseManager.removelease(lease);
            leaseTableModel.removelease(lease);
        } catch (Exception ex) {
            String msg = "Deleting failed";
            LOGGER.log(Level.INFO, msg);
        }
    }//GEN-LAST:event_leases_deleteActionPerformed

    private void sample_dragonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sample_dragonActionPerformed
        LOGGER.log(Level.INFO, "Adding sample dragon");
        dragon dragon = Sampler.createSampledragon();
        dragonManager.adddragon(dragon);
        dragonTableName.addDragon(dragon);
    }//GEN-LAST:event_sample_dragonActionPerformed

    private void sample_customerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sample_customerActionPerformed
        LOGGER.log(Level.INFO, "Adding sample customer");
        Customer customer = Sampler.createSampleCustomer();
        customerManager.addCustomer(customer);
        customerTableModel.addCustomer(customer);
    }//GEN-LAST:event_sample_customerActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
	/*
	 * Set the Nimbus look and feel
	 */
	//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
	 * If Nimbus (introduced in Java SE 6) is not available, stay with the
	 * default look and feel. For details see
	 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
	 */
	try {
	    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
		if ("Nimbus".equals(info.getName())) {
		    javax.swing.UIManager.setLookAndFeel(info.getClassName());
		    break;
		}
	    }
	} catch (ClassNotFoundException ex) {
	    java.util.logging.Logger.getLogger(DragonManagerApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	} catch (InstantiationException ex) {
	    java.util.logging.Logger.getLogger(DragonManagerApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	} catch (IllegalAccessException ex) {
	    java.util.logging.Logger.getLogger(DragonManagerApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	} catch (javax.swing.UnsupportedLookAndFeelException ex) {
	    java.util.logging.Logger.getLogger(DragonManagerApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
	}
	//</editor-fold>

	/*
	 * Create and display the form
	 */
	java.awt.EventQueue.invokeLater(new Runnable() {

	    public void run() {
		new DragonManagerApp().setVisible(true);
	    }
	});
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
