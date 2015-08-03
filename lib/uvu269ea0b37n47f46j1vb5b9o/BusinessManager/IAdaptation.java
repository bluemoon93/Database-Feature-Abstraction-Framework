package BusinessManager;

public interface IAdaptation {
	public void removeBusinessSchema(Class bs) throws BTC_Exception;
	public void addBusinessSchema(Class bs) throws BTC_Exception;
	public void repository(String jarFile, boolean Rebuild);
}