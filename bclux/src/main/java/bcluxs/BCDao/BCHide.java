package bcluxs.BCDao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BCHide {
    @JsonProperty("animal_type")
    Integer animalType;
    @JsonProperty("reserve_type")
    Integer reserveType;
    Integer producer;
    @JsonProperty("transaction_time")
    Long transactionTime;
}
