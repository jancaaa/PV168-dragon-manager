/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.*;
import cz.muni.fi.pv168.common.*;
import org.apache.derby.jdbc.EmbeddedDataSource;
import javax.sql.DataSource;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author strajk
 */
public class DragonManagerLay extends JFrame {

    private DataSource dataSource;
 
    private AppSwingWorker appSwingWorker;

    LeaseManager leaseManager;
    LeaseTableModel leaseTableModel;
    
    CustomerManager customerManager;
    CustomerTableModel customerTableModel;
    
    DragonManager dragonManager;
    DragonTableModel dragonTableModel;

    JPanel frame = new JPanel();

    JPanel header = new JPanel();
    JPanel content = new JPanel();
    JPanel footer = new JPanel();
    
    JPanel leases = new JPanel();
    JPanel customers = new JPanel();
    JPanel dragons = new JPanel();


    JScrollPane leases_scroll = new JScrollPane();
    JTable      leases_table = new JTable();
    JButton     leases_add = new JButton();
    JButton     leases_delete = new JButton();
    JButton     leases_update = new JButton();

    JScrollPane customers_scroll = new JScrollPane();
    JTable      customers_table = new JTable();
    JButton     customers_add = new JButton();
    JButton     customers_delete = new JButton();
    JButton     customers_update = new JButton();

    JScrollPane dragons_scroll = new JScrollPane();
    JTable      dragons_table = new JTable();
    JButton     dragons_add = new JButton();
    JButton     dragons_delete = new JButton();
    JButton     dragons_update = new JButton();


    JDialog    leases_dialog            = new JDialog();
    JLabel     leases_dialog_idLabel    = new JLabel();
    JTextField leases_dialog_idValue    = new JTextField();
    JLabel     leases_dialog_modelLabel = new JLabel();
    JTextField leases_dialog_modelValue = new JTextField();
    JLabel     leases_dialog_plateLabel = new JLabel();             // upravit?
    JTextField leases_dialog_plateValue = new JTextField();
    JLabel     leases_dialog_feeLabel   = new JLabel();
    JTextField leases_dialog_feeValue   = new JTextField();
    JButton    leases_dialog_submit     = new JButton();
    JButton    leases_dialog_cancel     = new JButton();


    JDialog    customers_dialog            = new JDialog();
    JLabel     customers_dialog_idLabel    = new JLabel();
    JTextField customers_dialog_idValue    = new JTextField();
    JLabel     customers_dialog_modelLabel = new JLabel();
    JTextField customers_dialog_modelValue = new JTextField();
    JLabel     customers_dialog_plateLabel = new JLabel();
    JTextField customers_dialog_plateValue = new JTextField();
    JLabel     customers_dialog_feeLabel   = new JLabel();
    JTextField customers_dialog_feeValue   = new JTextField();
    JButton    customers_dialog_submit     = new JButton();
    JButton    customers_dialog_cancel     = new JButton();

    JDialog    dragons_dialog            = new JDialog();
    JLabel     dragons_dialog_idLabel    = new JLabel();
    JTextField dragons_dialog_idValue    = new JTextField();
    JLabel     dragons_dialog_modelLabel = new JLabel();
    JTextField dragons_dialog_modelValue = new JTextField();
    JLabel     dragons_dialog_plateLabel = new JLabel();
    JTextField dragons_dialog_plateValue = new JTextField();
    JLabel     dragons_dialog_feeLabel   = new JLabel();
    JTextField dragons_dialog_feeValue   = new JTextField();
    JButton    dragons_dialog_submit     = new JButton();
    JButton    dragons_dialog_cancel     = new JButton();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DragonManagerLay app = new DragonManagerLay();
                app.setVisible(true);
            }
        });
    }

    public DragonManagerLay() {
        Locale locale_cs = new Locale("cs");
        Locale locale_en = new Locale("en");
        Locale locale_sk = new Locale("sk");
        Locale.setDefault(locale_cs);

	initComponents();
        
         dataSource = new EmbeddedDataSource();
        //we will use in memory database
        dataSource.setDatabaseName("memory:app-db");
        dataSource.setCreateDatabase("create");
        
        DBUtils.executeSqlScript(dataSource, DragonManagerLay.class.getResource("db_create.sql"));

        appSwingWorker = new AppSwingWorker();
        appSwingWorker.execute();
    }

    private class AppSwingWorker extends SwingWorker<Void, Object> {
        protected Void doInBackground() throws Exception {
            leaseTableModel = (LeaseTableModel) leases_table.getModel();
            customerTableModel = (CustomerTableModel) customers_table.getModel();
            dragonTableModel = (DragonTableModel) dragons_table.getModel();

            leaseManager = new LeaseManagerImpl(dataSource);
            customerManager = new CustomerManagerImpl(dataSource);
            dragonManager = new DragonManagerImpl(dataSource);

            for (Lease lease : leaseManager.findAllLeases()) {
                publish(lease);
            }
            for (Customer customer : customerManager.findAllCustomers()) {
                publish(customer);
            }
            for (Dragon dragon : dragonManager.findAllDragon()) {
                publish(dragon);
            }

            return null;
        }

        @Override
        protected void process(List<Object> items) {
            System.out.println("Process dragon");
            for (Object obj : items) {
                if (obj instanceof Lease) {
                    leaseTableModel.addLease((Lease) obj);
                } else if (obj instanceof Customer) {
                    customerTableModel.addCustomer((Customer) obj);
                } else if (obj instanceof Dragon) {
                    dragonTableModel.addDragon((Dragon) obj);
                } else {
                    String msg = "Undefined Object to process";
                    throw new IllegalArgumentException(msg);
                }
            }
        }
    }

    private void initComponents() {
        frame.setLayout(new BorderLayout());
        frame.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

        init_menu();
        init_header();
        init_content();
        init_footer();

        add(frame);
        pack();

        setTitle("Dragon manager"); //TODO Locale
        setSize(800, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void init_menu() { // TODO
        JMenuBar jMenuBar = new JMenuBar();
        JMenu jMenuApp = new JMenu("File");

        JMenuItem jMenuItemAppQuit = new JMenuItem("Quit");
        jMenuItemAppQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
            System.exit(0);
            }
        });
        jMenuApp.add(jMenuItemAppQuit);

        jMenuBar.add(jMenuApp);
        setJMenuBar(jMenuBar);
	
    }
    
    private void init_header() {
        header.setBackground(Color.gray);
        header.setPreferredSize(new Dimension(800, 50));
        frame.add(header, BorderLayout.NORTH);
    }
    
    private void init_content() {
        content.setPreferredSize(new Dimension(800, 600));
        content.setLayout(new BorderLayout());

        init_leases();
        init_customers();
        init_dragons();

        frame.add(content, BorderLayout.CENTER);
    }

    private void init_leases() {
        leases.setPreferredSize(new Dimension(700, 250));
        leases.setLayout(new BorderLayout());
        leases.setBackground(Color.red);

        leases_scroll.add(leases_table);
        leases.add(leases_scroll, BorderLayout.CENTER);
        leases.add(leases_add, BorderLayout.EAST);
        leases.add(leases_update, BorderLayout.EAST);
        leases.add(leases_delete, BorderLayout.EAST);

        content.add(leases, BorderLayout.CENTER);
    }

    private void init_customers() {
        customers.setLayout(new BorderLayout());
        customers.setBackground(Color.green);

        customers_add.setText("Add");
        customers_update.setText("Update");
        customers_delete.setText("Delete");

        customers_scroll.add(customers_table);
        customers.add(customers_scroll, BorderLayout.CENTER);
        customers.add(customers_add, BorderLayout.EAST);
        customers.add(customers_update, BorderLayout.EAST);
        customers.add(customers_delete, BorderLayout.EAST);

        frame.add(customers, BorderLayout.CENTER);
    }

    private void init_dragons() {
        dragons.setLayout(new BorderLayout());
        dragons.setBackground(Color.blue);

        dragons_scroll.add(dragons_table);
        dragons.add(dragons_scroll, BorderLayout.CENTER);
        dragons.add(dragons_add, BorderLayout.EAST);
        dragons.add(dragons_update, BorderLayout.EAST);
        dragons.add(dragons_delete, BorderLayout.EAST);

        frame.add(dragons, BorderLayout.CENTER);
    }

    private void init_footer() {
        JPanel container = new JPanel();

        JTextArea jTextAreaLog = new JTextArea("Log");
        jTextAreaLog.setPreferredSize(new Dimension(200, 50));
        container.add(jTextAreaLog);

        frame.add(container, BorderLayout.SOUTH);
    }

    private void initDialogDragon() {
        dragons_dialog.setPreferredSize(new Dimension(400, 400));
        dragons_dialog_idLabel.setPreferredSize(new Dimension(200, 28));
        dragons_dialog_idLabel.setText("id");
        dragons_dialog_idValue.setPreferredSize(new Dimension(200, 28));
        dragons_dialog_idValue.setEditable(false);
        dragons_dialog_modelLabel.setPreferredSize(new Dimension(200, 28));
        dragons_dialog_modelLabel.setText("name");
        dragons_dialog_modelValue.setPreferredSize(new Dimension(200, 28));
        dragons_dialog_plateLabel.setPreferredSize(new Dimension(200, 28));
        dragons_dialog_plateLabel.setText("cout of heads");
        dragons_dialog_plateValue.setPreferredSize(new Dimension(200, 28));
        dragons_dialog_feeLabel.setPreferredSize(new Dimension(200, 28));
        dragons_dialog_feeLabel.setText("price for day");
        dragons_dialog_feeValue.setPreferredSize(new Dimension(200, 28));
        dragons_dialog_submit.setPreferredSize(new Dimension(50, 30));
        dragons_dialog_submit.setText("Submit");
        dragons_dialog_cancel.setPreferredSize(new Dimension(50, 30));
        dragons_dialog_cancel.setText("Cancel");

        dragons_dialog_cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dragons_dialog.setVisible(false);
            }
        });

        dragons_dialog_submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Dragon dragon = new Dragon();
                if (! (dragons_dialog_idValue.getText()).isEmpty()) {
                    dragon.setId(Long.valueOf(dragons_dialog_idValue.getText()));
                };
                dragon.setName(dragons_dialog_modelValue.getText());
                dragon.setCountOfHeads(Integer.parseInt(dragons_dialog_plateValue.getText()));
                dragon.setPriceForDay(Integer.parseInt(dragons_dialog_feeValue.getText()));
                dragonManager.createDragon(dragon);
                dragonTableModel.addDragon(dragon);
                dragons_dialog.setVisible(false);
            }
        });

    }

    
   
    
}
