package net.g1park.throwingmoney.business.common.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ThrowTarget {
    private long seqNo;
    private int throwID;
    private int receiverID;
    private long dividedAmount;
    private String token;
    private int resultCode;

    private LocalDateTime insDate;
    private LocalDateTime updDate;

    public ThrowTarget(int throwID, int receiverID, long dividedAmount, String token, LocalDateTime insDate){
        this.throwID = throwID;
        this.receiverID = receiverID;
        this.dividedAmount = dividedAmount;
        this.token = token;
        this.resultCode = -1;
        this.insDate = insDate;
        this.updDate = null;
    }
}
