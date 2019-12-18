package bclux.bclux.config;

import bclux.bclux.BCRepository.FabricEnrollment;
import bclux.bclux.BCRepository.FabricSDK;
import bclux.bclux.BCRepository.FabricUser;
import lombok.Getter;
import lombok.Setter;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashSet;

@Configuration
@ConfigurationProperties(prefix = "fabric.config")
@Getter
@Setter
public class FabricConfig {
    String cert;
    String key;
    String ipAddress;

    @Bean
    public FabricEnrollment enrollment() throws NoSuchAlgorithmException {
        FabricEnrollment enrollment = new FabricEnrollment();
        enrollment.setCert(cert);
        byte[] pkBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkBytes);
        KeyFactory factory = KeyFactory.getInstance("EC");
        try {
            PrivateKey privateKey = factory.generatePrivate(keySpec);
            enrollment.setKey(privateKey);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return enrollment;
    }

    @Bean
    public FabricUser user() throws NoSuchAlgorithmException {
        FabricUser user = new FabricUser();
        user.setEnrollment(enrollment());
        HashSet<String> roles = new HashSet<>();
        roles.add("member");
        user.setRoles(roles);
        return user;
    }

    @Bean
    public FabricSDK fabricSDK() throws IllegalAccessException, InvocationTargetException, InvalidArgumentException, InstantiationException, NoSuchMethodException, CryptoException, ClassNotFoundException, NoSuchAlgorithmException, TransactionException {
        // 设置client
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        client.setUserContext(user());

        // 设置通道
        Orderer orderer1 = client.newOrderer("orderer.Brand", "grpc://10.185.41.224:7050");
        Orderer orderer2 = client.newOrderer("orderer.Brand", "grpc://10.185.41.224:7050");
        Orderer orderer3 = client.newOrderer("orderer.Brand", "grpc://10.185.41.224:7050");
        Orderer orderer4 = client.newOrderer("orderer.Brand", "grpc://10.185.41.224:7050");
        Peer peer1 = client.newPeer("peer0.Brand", "grpc://10.185.41.224:7051");
        Peer peer2 = client.newPeer("peer0.Brand", "grpc://10.185.41.224:7051");
        Peer peer3 = client.newPeer("peer0.Brand", "grpc://10.185.41.224:7051");
        Peer peer4 = client.newPeer("peer0.Brand", "grpc://10.185.41.224:7051");
        Channel up = client.newChannel("upstreamchannel");
        Channel down = client.newChannel("downstreamchannel");
        Channel market = client.newChannel("marketchannel");
        Channel query = client.newChannel("querychannel");
        up.addOrderer(orderer1).addPeer(peer1).initialize();
        down.addOrderer(orderer2).addPeer(peer2).initialize();
        market.addOrderer(orderer3).addPeer(peer3).initialize();
        query.addOrderer(orderer4).addPeer(peer4).initialize();

        // 设置请求
        ChaincodeID upstreamCC = ChaincodeID.newBuilder().setName("UpstreamCC").build();
        ChaincodeID downstreamCC = ChaincodeID.newBuilder().setName("DownstreamCC").build();
        ChaincodeID marketCC = ChaincodeID.newBuilder().setName("MarketCC").build();
        ChaincodeID queryCC = ChaincodeID.newBuilder().setName("QueryCC").build();
        QueryByChaincodeRequest upstreamQuery = client.newQueryProposalRequest();
        upstreamQuery.setChaincodeID(upstreamCC);
        upstreamQuery.setFcn("query");
        QueryByChaincodeRequest downstreamQuery = client.newQueryProposalRequest();
        downstreamQuery.setChaincodeID(downstreamCC);
        downstreamQuery.setFcn("query");
        QueryByChaincodeRequest marketQuery = client.newQueryProposalRequest();
        marketQuery.setChaincodeID(marketCC);
        marketQuery.setFcn("query");
        TransactionProposalRequest queryRequest = client.newTransactionProposalRequest();
        queryRequest.setChaincodeID(queryCC);
        TransactionProposalRequest marketRequest = client.newTransactionProposalRequest();
        marketRequest.setChaincodeID(marketCC);
        TransactionProposalRequest downstreamRequest = client.newTransactionProposalRequest();
        downstreamRequest.setChaincodeID(downstreamCC);
        downstreamRequest.setFcn("invoke");
        TransactionProposalRequest upstreamRequest = client.newTransactionProposalRequest();
        upstreamRequest.setChaincodeID(upstreamCC);
        upstreamRequest.setFcn("invoke");

        // 设置最终的sdk
        FabricSDK sdk = new FabricSDK();
        sdk.setClient(client);
        sdk.setUpstreamChannel(up);
        sdk.setDownstreamChannel(down);
        sdk.setMarketChannel(market);
        sdk.setQueryChannel(query);
        sdk.setUpstreamQuery(upstreamQuery);
        sdk.setDownstreamQuery(downstreamQuery);
        sdk.setMarketQuery(marketQuery);
        sdk.setQueryRequest(queryRequest);
        sdk.setMarketRequest(marketRequest);
        sdk.setDownstreamRequest(downstreamRequest);
        sdk.setUpstreamRequest(upstreamRequest);
        return sdk;
    }
}
