package BusinessManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Implements a sequence.
 * 
 * Created by Regateiro on 24-02-2014.
 */
public class SequenceEntry {
    private final String authorizedBS;
    private final List<String> revokeList;
    private final List<Integer> authorizedCRUDs;

    public SequenceEntry(String authorizedBS, List<String> revokeList, List<Integer> cruds) {
        this.authorizedBS = authorizedBS;
        this.revokeList = revokeList;
        this.authorizedCRUDs = cruds;
    }

    public String getAuthorizedBS() {
        return authorizedBS;
    }

    public List<String> getRevokeList() {
        return revokeList;
    }

    public List<Integer> getAuthorizedCRUDs() {
        return authorizedCRUDs;
    }
}
