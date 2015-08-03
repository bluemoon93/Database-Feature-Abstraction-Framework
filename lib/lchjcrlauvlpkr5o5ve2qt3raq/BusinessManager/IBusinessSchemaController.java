package BusinessManager;

import java.util.List;

public interface IBusinessSchemaController {
	public boolean validateExecution(int activeSequence, String beUrl);
	public boolean authorize(int activeSequence, String beUrl);
	public void addBEtoSequence(int seq, String beUrl, List<String> revokeList);
	public void removeSequence(int seq);
	public int requestNewActiveSequence();
	public void setControlStatus(boolean active);
	public void clearControl();}