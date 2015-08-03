package example;

import BusinessManager.BusinessManager;
import BusinessManager.IBusinessManager;
import BusinessManager.ISession;
import Classes.Role_IRole_B1;
import I_Orders.II_Orders;
import S_Customers_byName.IS_Customers2;
import LocalTools.BTC_Exception;
import S_Customers.IS_Customers;
import S_Orders.IS_Orders;
import U_Orders.IU_Orders;
import Security.AuthenticationMethod;
import annotation.DACAManagedApplication;
import static LocalTools.MessageTypes.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

@DACAManagedApplication(
        username = "user1",
        password = "guest",
        port = 9001,
        app = "app",
        authenticationMethod = AuthenticationMethod.ChallengeResponse, 
        keyStorePath = "c:\\certs\\myClientKeystore",
        keyStorePassword = "123456",
        updatePolicies = true,
        includePolicies = true
)
public class Example {
    //Schemas declaration

    private static IS_Customers S_Cust_1, S_Cust_2;
    private static IS_Customers2 S_Cust2;
    private static IS_Orders S_Orders;
    private static II_Orders I_Orders;
    private static IU_Orders U_Orders;
    private static IBusinessManager manager;
    private static Factory factory;
    private static ISession session;

    public static void main(String[] args) throws BTC_Exception, IOException, SQLException, NoSuchAlgorithmException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        //Configure Environment and Load BusinessSchemas
        ConfigureEnvironment();

        do {
            try {
                doIt();
            } catch (IllegalStateException ex) {
                System.out.println("Somewhere the method has been denied execution.");
                System.out.println("Details: " + ex.getMessage());
            }
        } while (!in.readLine().equalsIgnoreCase("q"));
        
        try {
            session.close();
        } catch (Exception e) {
        }

        try {
            manager.close();
        } catch (Exception e) {
        }

    }

    private static void doIt() throws BTC_Exception, SQLException {
        //S_Cust2 = manager.instantiateBS(Role_IRole_B1.s_customers2, Role_IRole_B1.s_customers2_S_Customers_byName, session);
        //Execute Customers Schema and select the first one
        S_Cust_1 = manager.instantiateBS(Role_IRole_B1.s_customers, Role_IRole_B1.s_customers_S_Custumers_all, session);
       
        // Selecting customers
        S_Cust_1.execute();
        System.out.println(" --> S_Cust has NOT been denied execution.\n");
        S_Cust_1.moveNext();
        
        //Prints to the screen associated information with the selected customer
        System.out.println("Selected Customer Info:\n");
        printSelectedCustomerInfo();
        System.out.println(" --> S_Cust has NOT been denied execution.\n");
        
        // Get next query in the sequence
        S_Orders = S_Cust_1.nextBE_S1(Role_IRole_B1.s_orders_S_Orders_byShipCountry, session);
        System.out.println("Got next query in the sequence");
        
        //Prints to the screen a list about orders made to "Country"
        S_Orders.set(S_Cust_1, "Country");
        S_Orders.execute();
        System.out.println(" --> S_Orders has NOT been denied execution.\n");
        System.out.println("Orders made to Country:\n");
        printOrderInfo();

        // Get another sequence, select a customer, get the next query in the sequence
        S_Cust_2 = factory.get_S_Customers_all(session);
        S_Cust_2.execute();
        S_Cust_2.moveNext();
        I_Orders = S_Cust_2.nextBE_S2(Role_IRole_B1.i_orders_I_Orders_withCostumerID, session);

        session.setAutoCommit(false);
        
        //Insert new Order for the current customer
        Calendar cal = new GregorianCalendar(2013, 1, 4);
        I_Orders.execute(S_Cust_2, 1, new Date(cal.getTimeInMillis()), new Date(cal.getTimeInMillis()), new Date(0), 1, 2, "ShipName", "Address", "City", "Region", "ZipCode", "Country");
        System.out.println(" --> I_Orders has NOT been denied execution.\n");
        System.out.println("Inserting new order to Country!\n");

        //Prints to the screen a list about orders made to country Country
        S_Orders.execute(S_Cust_1, "Country");
        System.out.println(" --> S_Orders has NOT been denied execution.\n");
        System.out.println("Orders made to Country:\n");
        printOrderInfo();
        
        System.out.println("Rolling back!");
        session.rollBack();
        
        
        S_Orders.execute(S_Cust_1, "Country");
        System.out.println(" --> S_Orders has NOT been denied execution.\n");
        System.out.println("Orders made to Country:\n");
        printOrderInfo();
        
        
        I_Orders.execute(S_Cust_2, 1, new Date(cal.getTimeInMillis()), new Date(cal.getTimeInMillis()), new Date(0), 1, 2, "ShipName", "Address", "City", "Region", "ZipCode", "Country");
        System.out.println(" --> I_Orders has NOT been denied execution.\n");
        System.out.println("Inserting new order to Country!\n");
        
        S_Orders.execute(S_Cust_1, "Country");
        System.out.println(" --> S_Orders has NOT been denied execution.\n");
        System.out.println("Orders made to Country:\n");
        printOrderInfo();
        
        System.out.println("Committing!");
        session.commit();
        
        session.setAutoCommit(true);

        // Updating an order
        System.out.println("Changing one of the orders to another country (U_Orders)!");
        S_Orders.execute(S_Cust_1, "Country");
        S_Orders.moveNext();
        
        U_Orders = S_Orders.nextBE_S1(Role_IRole_B1.u_orders_U_Orders_shipDestination, session);
        U_Orders.execute(S_Orders, "NewAddress","NewCity","NewCountry");
        System.out.println("\tU_Orders has NOT been denied execution.\n");

        // Show the orders again
        S_Orders.execute(S_Cust_1, "NewCountry");
        System.out.println("Orders made to Country:\n");
        printOrderInfo();
        
        System.out.println("Updating a row!");
        S_Orders.execute(S_Cust_1, "NewCountry");
        S_Orders.moveNext();
        S_Orders.beginUpdate();
        S_Orders.uShipName("Random"+(new Random().nextInt()%10));
        S_Orders.updateRow();
        
        S_Orders.execute(S_Cust_1, "NewCountry");
        System.out.println("Orders made to Country:\n");
        printOrderInfo();
    }
    
    /*
    
    
    */

    //Configure Environment and Load BusinessSchemas
    public static void ConfigureEnvironment() throws BTC_Exception, IOException, SQLException, NoSuchAlgorithmException {
        System.out.println("Configuring");
        String url = "sqlserver://" + "192.168.57.101:1433" + ";" + "database=" + "Northwind" + ";";
        
        BusinessManager.configure("user1", "guest", "localhost", 9001, "app", AuthenticationMethod.ChallengeResponse, "c:\\certs\\myClientKeystore", "123456", true);
        System.out.println("Configuring 2");
        manager = BusinessManager.getInstance();
        session = manager.getSession(url);
        factory = new Factory(manager);
        System.out.println("Done config");
    }

    //Prints the information about the selected customer
    public static void printSelectedCustomerInfo() throws SQLException {

        printWithSpaces("Company Name", 20);
        printWithSpaces("Contact Name", 20);
        printWithSpaces("Contact Title", 20);
        printWithSpaces("Address", 20);
        printWithSpaces("City", 10);
        printWithSpaces("Region", 10);
        printWithSpaces("Postal Code", 10);
        printWithSpaces("Country", 10);
        printWithSpaces("Phone", 20);
        printWithSpaces("Fax", 10);
        System.out.println("|");
        printWithSpaces(S_Cust_1.CompanyName(), 20);
        printWithSpaces(S_Cust_1.ContactName(), 20);
        printWithSpaces(S_Cust_1.ContactTitle(), 20);
        printWithSpaces(S_Cust_1.Address(), 20);
        printWithSpaces(S_Cust_1.City(), 10);
        printWithSpaces(S_Cust_1.Region(), 10);
        printWithSpaces(S_Cust_1.PostalCode(), 10);
        printWithSpaces(S_Cust_1.Country(), 10);
        printWithSpaces(S_Cust_1.Phone(), 20);
        printWithSpaces(S_Cust_1.Fax(), 10);
        System.out.println("|\n");
    }

    //Prints to the screen a list of orders
    public static void printOrderInfo() throws SQLException {

        printWithSpaces("OrderID", 10);
        printWithSpaces("CustomerID", 10);
        printWithSpaces("EmployeeID", 10);
        printWithSpaces("OrderDate", 20);
        printWithSpaces("RequiredDate", 20);
        printWithSpaces("ShippedDate", 20);
        printWithSpaces("ShipName", 20);
        printWithSpaces("ShipCountry", 20);
        System.out.println("|");
        while (S_Orders.moveNext()) {
            printWithSpaces(Integer.toString(S_Orders.OrderID()), 10);
            printWithSpaces(S_Orders.CustomerID(), 10);
            printWithSpaces(Integer.toString(S_Orders.EmployeeID()), 10);
            printWithSpaces(S_Orders.OrderDate().toString(), 20);
            printWithSpaces(S_Orders.RequiredDate().toString(), 20);
            printWithSpaces(S_Orders.ShippedDate().toString(), 20);
            printWithSpaces(S_Orders.ShipName(), 20);
            printWithSpaces(S_Orders.ShipCountry(), 20);
            System.out.println("|");
        }
        System.out.println();
    }

    public static String Center(String text, int len) {
        String out = String.format("%" + len + "s%s%" + len + "s", "", text, "");
        float mid = (out.length() / 2);
        float start = mid - (len / 2);
        float end = start + len;
        return out.substring((int) start, (int) end);
    }

    public static void printWithSpaces(String word, int size) {
        System.out.printf("|" + Center(word, size));
    }
    
    private static void doItHive() throws BTC_Exception, SQLException {
        //Execute Customers Schema and select the first one
        S_Cust_1 = manager.instantiateBS(Role_IRole_B1.s_customers, Role_IRole_B1.s_customers_S_Custumers_all, session);
        S_Cust_1.execute();
        System.out.println(" --> S_Cust has NOT been denied execution.\n");
        S_Cust_1.moveNext();

        //Prints to the screen associated information with the selected customer
        System.out.println("Selected Customer Info:\n");
        printSelectedCustomerInfo();
        
        System.out.println(" --> S_Cust has NOT been denied execution.\n");
        //Prints to the screen a list about orders made to Portugal
        S_Orders = S_Cust_1.nextBE_S1(Role_IRole_B1.s_orders_S_Orders_byShipCountry, session);
        S_Orders.execute(S_Cust_1, "Country");
        System.out.println(" --> S_Orders has NOT been denied execution.\n");
        System.out.println("Orders made to USA:\n");
        printOrderInfo();

        Calendar cal = new GregorianCalendar(2013, 1, 4);
        S_Orders.beginInsert();
        S_Orders.iCustomerID(S_Cust_1.CustomerID());
        S_Orders.iEmployeeID(1);
        S_Orders.iOrderDate(new Date(cal.getTimeInMillis()));
        S_Orders.iRequiredDate(new Date(cal.getTimeInMillis()));
        S_Orders.iShippedDate(new Date(0));
        S_Orders.iShipVia(1);
        S_Orders.iFreight(2);
        S_Orders.iShipName("Ship Name");
        S_Orders.iShipAddress("Address");
        S_Orders.iShipCity("City");
        S_Orders.iShipRegion("Region");
        S_Orders.iShipPostalCode("Zip Code");
        S_Orders.iShipCountry("Country");
        S_Orders.endInsert(true);
        
        S_Orders.execute(S_Cust_1, "Country");
        System.out.println(" --> S_Orders has NOT been denied execution.\n");
        System.out.println("Orders made to USA:\n");
        printOrderInfo();
        
        S_Orders.execute(S_Cust_1, "Country");
        S_Orders.moveNext();
//        S_Orders.deleteRow();
        S_Orders.beginUpdate();
        S_Orders.uShipName("New Name");
        S_Orders.updateRow();
        
        S_Orders.execute(S_Cust_1, "Country");
        System.out.println(" --> S_Orders has NOT been denied execution.\n");
        System.out.println("Orders made to USA:\n");
        printOrderInfo();
    }

}
