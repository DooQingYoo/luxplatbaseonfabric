package bclux.bclux.BCRepository;

import lombok.Setter;
import org.hyperledger.fabric.sdk.Enrollment;

import java.security.PrivateKey;

@Setter
public class FabricEnrollment implements Enrollment {
    private String cert;
    private PrivateKey key;
    @Override
    public PrivateKey getKey() {
        return key;
    }

    @Override
    public String getCert() {
        return cert;
    }
}
