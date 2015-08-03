package S_Customers;
import java.math.*;
import java.sql.*;

import S_Customers.IRCustomerID;
public interface IRead extends IRCustomerID {

String Country() throws SQLException;

String Address() throws SQLException;

String City() throws SQLException;

String Region() throws SQLException;

String Phone() throws SQLException;

String Fax() throws SQLException;

String CompanyName() throws SQLException;

String ContactName() throws SQLException;

String ContactTitle() throws SQLException;

String PostalCode() throws SQLException;
}