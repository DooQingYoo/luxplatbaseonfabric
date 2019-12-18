package bclux.bclux.BCRepository;

import bclux.bclux.BCDao.BCCommodity;
import bclux.bclux.BCDao.BCHide;
import bclux.bclux.BCDao.BCLeather;
import bclux.bclux.BCDao.BCSoldCommodity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Getter
@Setter
public class FabricSDK {
    private HFClient client;
    private Channel upstreamChannel;
    private Channel downstreamChannel;
    private Channel marketChannel;
    private Channel queryChannel;
    private TransactionProposalRequest queryRequest;
    private TransactionProposalRequest marketRequest;
    private TransactionProposalRequest downstreamRequest;
    private TransactionProposalRequest upstreamRequest;
    private QueryByChaincodeRequest marketQuery;
    private QueryByChaincodeRequest downstreamQuery;
    private QueryByChaincodeRequest upstreamQuery;
    private ObjectMapper mapper = new ObjectMapper();

    // 提取出查询方法的公共部分
    private String commonQuery(String serialNum, QueryByChaincodeRequest request, Channel channel) {
        request.setArgs(serialNum);
        Collection<ProposalResponse> responses;
        try {
            responses = channel.queryByChaincode(request);
        } catch (InvalidArgumentException | ProposalException e) {
            e.printStackTrace();
            return null;
        }
        ProposalResponse response = responses.iterator().next();
        byte[] payload;
        try {
            payload = response.getChaincodeActionResponsePayload();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
            return null;
        }
        return new String(payload);
    }

    // 提取出执行方法的公共部分
    private String commonInvoke(TransactionProposalRequest request, Channel channel) {
        Collection<ProposalResponse> responses;
        try {
            responses = channel.sendTransactionProposal(request);
        } catch (ProposalException | InvalidArgumentException e) {
            e.printStackTrace();
            return null;
        }
        ProposalResponse response = responses.iterator().next();
        if (response.isInvalid() || !response.isVerified()) {
            return null;
        }
        byte[] payload;
        try {
            payload = response.getChaincodeActionResponsePayload();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
            return null;
        }
        if (payload == null) {
            return null;
        }
        CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture = channel.sendTransaction(responses);
        String ret = new String(payload);
        BlockEvent.TransactionEvent transactionEvent;
        try {
            transactionEvent = transactionEventCompletableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
        if (transactionEvent.isValid()) {
            return ret;
        } else {
            System.out.println(transactionEvent.getValidationCode());
            return null;
        }
    }

    // 根据序列号在区块链中查询生皮
    public BCHide queryHide(String serialNum) {
        String jsonStr = commonQuery(serialNum, upstreamQuery, upstreamChannel);
        if (jsonStr == null) {
            return null;
        }
        BCHide bcHide;
        try {
            bcHide = mapper.readValue(jsonStr, BCHide.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        return bcHide;
    }

    // 根据序列号在区块链中查询皮革
    public BCLeather queryLeather(String serialNum) {
        String jsonStr = commonQuery(serialNum, downstreamQuery, downstreamChannel);
        if (jsonStr == null) {
            return null;
        }
        BCLeather bcLeather;
        try {
            bcLeather = mapper.readValue(jsonStr, BCLeather.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        return bcLeather;
    }

    // 根据序列号在区块链中查询商品（批次）
    public BCCommodity queryCommodity(String serialNum) {
        String jsonStr = commonQuery(serialNum, marketQuery, marketChannel);
        if (jsonStr == null) {
            return null;
        }
        BCCommodity bcCommodity;
        try {
            bcCommodity = mapper.readValue(jsonStr, BCCommodity.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        return bcCommodity;
    }

    // 根据防伪码查询区块链里的已卖出的商品
    public BCSoldCommodity querySold(String serialNum) {
        queryRequest.setFcn("query");
        queryRequest.setArgs(serialNum);
        Collection<ProposalResponse> responses;
        try {
            responses = queryChannel.sendTransactionProposal(queryRequest);
        } catch (ProposalException | InvalidArgumentException e) {
            e.printStackTrace();
            return null;
        }
        ProposalResponse response = responses.iterator().next();
        if (response.isInvalid() || !response.isVerified()) {
            return null;
        }
        byte[] payload;
        try {
            payload = response.getChaincodeActionResponsePayload();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
            return null;
        }
        if (payload == null) {
            return null;
        }
        CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture = queryChannel.sendTransaction(responses);
        BCSoldCommodity bcSoldCommodity;
        try {
            bcSoldCommodity = mapper.readValue(new String(payload), BCSoldCommodity.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        // 如果之前没被查询过才需要给orderer发送写账本请求，否则就不用发送了
        if (!bcSoldCommodity.getQueried()) {
            CompletableFuture<Exception> ex = transactionEventCompletableFuture.handle((event, exception) -> {
                // 不成功就再发一次，再不成功就算了
                if (exception != null || !event.isValid()) {
                    assert exception != null;
                    exception.printStackTrace();
                    CompletableFuture<BlockEvent.TransactionEvent> newFuture = queryChannel.sendTransaction(responses);
                    try {
                        BlockEvent.TransactionEvent transactionEvent = newFuture.get();
                        if (!transactionEvent.isValid()) {
                            return new Exception("第二次仍旧失败: TxValidationCode为: " + transactionEvent.getValidationCode());
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        return e;
                    }
                }
                return null;
            });
            try {
                Exception exception = ex.get();
                if (exception != null) {
                    exception.printStackTrace();
                    return null;
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return null;
            }
        }
        return bcSoldCommodity;
    }

    // 在查询通道执行sell方法
    public String querychannelSell(String id, String commoditySerial) {
        queryRequest.setFcn("sell");
        queryRequest.setArgs(id, commoditySerial);
        return commonInvoke(queryRequest, queryChannel);
    }

    // 在销售通道执行sell方法
    public boolean marketchannelSell(String serialNum) {
        marketRequest.setFcn("sell");
        marketRequest.setArgs(serialNum, "1");
        String res = commonInvoke(marketRequest, marketChannel);
        return "Selling Success".equals(res);
    }

    // 执行某一批次商品已被生产出来
    public String produceCommodity(String factory, String totalCount, String leatherSerial) {
        marketRequest.setFcn("invoke");
        marketRequest.setArgs(factory, totalCount, leatherSerial);
        return commonInvoke(marketRequest, marketChannel);
    }

    // 某批次皮革已被生产出来
    public String produceLeather(String tanning, String layer, String producer, String hideSerial) {
        downstreamRequest.setArgs(tanning, layer, producer, hideSerial);
        return commonInvoke(downstreamRequest, downstreamChannel);
    }

    // 某批次生皮已被生产出来
    public String produceHide(String animalType, String reserveType, String producer) {
        upstreamRequest.setArgs(animalType, reserveType, producer);
        return commonInvoke(upstreamRequest, upstreamChannel);
    }
}