package net.g1park.throwingmoney.business.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ThrowEvent {
    protected int throwID;
    protected String roomID;
    protected int ownerID;
    protected long totalAmount;
    protected int targetCount;
    protected String token;
    protected String accountNo;
    protected LocalDateTime insDate;
    protected LocalDateTime updDate;

    public ThrowEvent(String roomID, int ownerID, long totalAmount, int targetCount){
        this.roomID = roomID;
        this.ownerID = ownerID;
        this.totalAmount = totalAmount;
        this.targetCount = targetCount;

        this.insDate = LocalDateTime.now();
        this.updDate = null;
    }
}
