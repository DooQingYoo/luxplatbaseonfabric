package bclux.bclux;

import bclux.bclux.BCRepository.FabricSDK;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BcluxApplicationTests {

    @Autowired
    FabricSDK sdk;
    @Test
    void contextLoads() {
        String s = sdk.produceCommodity("3", "500", "G3ktegf+OEcZJ31jjXuYX8daxHUeSqZCgeViJVPIxKA");
        System.out.println(s);
    }

}
