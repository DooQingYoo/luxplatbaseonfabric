package bclux.bclux.BCRepository;

import lombok.Getter;
import lombok.Setter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.util.Set;

@Getter
@Setter
public class FabricUser implements User {

    Set<String> roles;
    FabricEnrollment enrollment;

    @Override
    public String getName() {
        return "User1";
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String getAccount() {
        return "";
    }

    @Override
    public String getAffiliation() {
        return "";
    }

    @Override
    public Enrollment getEnrollment() {
        return enrollment;
    }

    @Override
    public String getMspId() {
        return "BrandMSP";
    }
}