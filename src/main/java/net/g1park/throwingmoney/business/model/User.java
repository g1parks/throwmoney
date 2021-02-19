package net.g1park.throwingmoney.business.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private int userID;
    private String userName;
    private int status;
    private LocalDateTime insDate;
    private LocalDateTime updDate;

}
