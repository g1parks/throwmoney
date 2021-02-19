package net.g1park.throwingmoney.business.model.api;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResponseGetThrowMoneyInfo {
    private LocalDateTime throwEventTime;
    private long amountOfMoeny;
    private long sumOfReceivedMoney;
    private List<Integer> receiverIDList;

}
