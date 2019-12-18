package bclux.bclux.BCDao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BCLeather {
    Integer tanning;
    Integer layer;
    Integer producer;
    @JsonProperty("transaction_time")
    Long transactionTime;
    @JsonProperty("hide_num")
    String hideNum;
}
