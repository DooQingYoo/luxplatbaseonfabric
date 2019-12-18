package bclux.bclux.BCDao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BCCommodity {
    Integer factory;
    @JsonProperty("total_count")
    Integer totalCount;
    @JsonProperty("transaction_time")
    Long transactionTime;
    Integer unsold;
    @JsonProperty("leather_num")
    String leatherNum;
}
