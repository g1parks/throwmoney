package net.g1park.throwingmoney.business.model;

import lombok.Data;
import java.time.LocalDateTime;

// 큐사용을 위해 설계했으나 시간 부족으로 미사용.
@Data
public class Transfer {
    private long SeqNo;
    private long fromAcctNo;
    private long toAcctNo;
    private long amount;
    private int resultCode;
    LocalDateTime insDate;
    LocalDateTime updDate;
}
