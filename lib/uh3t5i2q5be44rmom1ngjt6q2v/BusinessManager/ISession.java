package BusinessManager;

public interface ISession extends ITransaction, AutoCloseable {
	public void getConnection();
}