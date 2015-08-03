package S_Customers_byName;
import java.math.*;
import java.sql.*;

public interface IInsert {
 void beginInsert() throws SQLException;
 void endInsert(boolean moveToPreviousRow) throws SQLException;
 void cancelInsert() throws SQLException;

void iCompanyName(String CompanyName) throws SQLException;

void iContactName(String ContactName) throws SQLException;

void iContactTitle(String ContactTitle) throws SQLException;

void iAddress(String Address) throws SQLException;

void iCity(String City) throws SQLException;

void iRegion(String Region) throws SQLException;

void iPostalCode(String PostalCode) throws SQLException;

void iCountry(String Country) throws SQLException;

void iPhone(String Phone) throws SQLException;

void iFax(String Fax) throws SQLException;
}