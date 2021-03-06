package bcluxs.BCDao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
public class BCSoldCommodity {
    Integer retailer;
    @JsonProperty("transaction_time")
    Long transactionTime;
    @JsonProperty("com_num")
    String commNum;
    @JsonProperty("query_times")
    Integer queryTimes;
    @JsonProperty("last_query")
    Long lastQuery;
}