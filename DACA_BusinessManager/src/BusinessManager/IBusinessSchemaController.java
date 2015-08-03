package BusinessManager;

import java.util.List;


/**
 * Interface of the Orquestrator class that authorizes BEs to be instantiated.
 *
 * @author Diogo Regateiro diogoregateiro@ua.pt
 */
public interface IBusinessSchemaController {
    /**
     * Validates the execution of a method of a BE.
     *
     * @param activeSequence The active sequence identifier.
     * @param beUrl          The unique name of the BE to have the execution validated.
     * @return True if the execution of the BE is valid, false otherwise.
     */
    public boolean validateExecution(int activeSequence, String beUrl);

    /**
     * Tried to authorize a Business Schema for execution.
     *
     * @param activeSequence The active sequence identifier.
     * @param beUrl          The unique name of the BE to be authorized.
     * @return True if the usage of the BE is authorized, false otherwise.
     */
    public boolean authorize(int activeSequence, String beUrl);

    /**
     * Adds a BE to the end of the sequence identified by seq.
     *
     * @param seq        The sequence identifier.
     * @param beUrl      The unique name of the BE to be authorized.
     * @param revokeList The list of beUrl that must be revoked (make impossible to use) after this beUrl is activated.
     */
    public void addBEtoSequence(int seq, String beUrl, List<String> revokeList);

    /**
     * Removes the sequence from the authorization chain.
     *
     * @param seq The sequence identifier.
     */
    public void removeSequence(int seq);

    /**
     * Determines a new identifier for an active sequence.
     *
     * @return The newly generated active sequence identifier.
     */
    public int requestNewActiveSequence();

    /**
     * Sets the orchestrator active or inactive. If inactive all authorization and validation requests pass through.
     *
     * @param active True to set the orchestrator to active, false to set it inactive.
     */
    public void setControlStatus(boolean active);

    /**
     * Removes every sequence from the authorization chain.
     */
    public void clearControl();
}
