package net.g1park.throwingmoney.business.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Room {
    private long seqNo;
    private String roomID;
    private String roomDesc;
    private int ownerID;
    private LocalDateTime insDate;
    private LocalDateTime updDate;
}



