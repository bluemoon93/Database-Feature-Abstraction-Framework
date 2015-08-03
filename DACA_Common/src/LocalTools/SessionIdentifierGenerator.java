package LocalTools;

import java.math.BigInteger;
import java.security.SecureRandom;

public abstract class SessionIdentifierGenerator {

    private static final SecureRandom random = new SecureRandom();

    public static String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }

}
