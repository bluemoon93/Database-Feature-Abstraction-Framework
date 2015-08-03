package BusinessManager;

import java.sql.*;

public interface IUser {
	public void addAndCallDACAChangeListener(DACAChangeListener listener);
	public void addDACAChangeListener(DACAChangeListener listener);
	public void removeDACAChangeListener(DACAChangeListener listener);
	public <T> T instantiateBS(Class<T> bs, int crudId, ISession session) throws BTC_Exception;
	public <T> T instantiateBS(Class<T> bs, int crudId, ISession session, Integer activeSequence) throws BTC_Exception;
	public ISession getSession(String dburl) throws SQLException;
}