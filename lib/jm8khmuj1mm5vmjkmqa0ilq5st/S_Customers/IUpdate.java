package S_Customers;
import java.math.*;
import java.sql.*;

public interface IUpdate {
 void beginUpdate() throws SQLException;
 void updateRow() throws SQLException;
 void cancelUpdate() throws SQLException;

void uPostalCode(String PostalCode) throws SQLException;

void uCountry(String Country) throws SQLException;

void uPhone(String Phone) throws SQLException;

void uFax(String Fax) throws SQLException;

void uCompanyName(String CompanyName) throws SQLException;

void uContactName(String ContactName) throws SQLException;

void uContactTitle(String ContactTitle) throws SQLException;

void uAddress(String Address) throws SQLException;

void uCity(String City) throws SQLException;

void uRegion(String Region) throws SQLException;
}