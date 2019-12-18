package bclux.bclux.BCDao;

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
    Boolean queried;
}