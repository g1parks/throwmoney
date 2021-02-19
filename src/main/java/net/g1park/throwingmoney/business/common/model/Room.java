package net.g1park.throwingmoney.business.common.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Room {
    private long seqNo;
    private String roomID;
    private String roomDesc;
    private int ownerID;
    private LocalDateTime insDate;
    private LocalDateTime updDate;
}



