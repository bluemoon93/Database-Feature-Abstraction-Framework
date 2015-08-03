package BusinessManager;

import java.util.HashMap;
import java.util.Map;

public interface DACAChangeListener {
	public void sequenceStatusChanged(boolean status);
	public void sequenceChanged(Map<Integer, Map<Integer, String>> sequences);
	public void policiesChanged(Map<String, HashMap<Integer, String>> businessSchemas);
}
