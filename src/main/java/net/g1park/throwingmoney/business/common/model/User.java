package net.g1park.throwingmoney.business.common.model;

import lombok.Data;
import java.time.LocalDateTime;

// 미사용
@Data
public class User {
    private int userID;
    private String userName;
    private int status;
    private LocalDateTime insDate;
    private LocalDateTime updDate;

}
