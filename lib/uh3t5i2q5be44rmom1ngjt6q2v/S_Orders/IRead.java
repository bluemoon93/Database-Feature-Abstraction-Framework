package S_Orders;
import java.math.*;
import java.sql.*;

import S_Orders.IROrderID;
public interface IRead extends IROrderID {

String ShipName() throws SQLException;

String CustomerID() throws SQLException;

int EmployeeID() throws SQLException;

Date OrderDate() throws SQLException;

Date RequiredDate() throws SQLException;

Date ShippedDate() throws SQLException;

String ShipCountry() throws SQLException;

String ShipRegion() throws SQLException;

double Freight() throws SQLException;

String ShipAddress() throws SQLException;

String ShipCity() throws SQLException;

int ShipVia() throws SQLException;

String ShipPostalCode() throws SQLException;
}