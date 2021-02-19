package net.g1park.throwingmoney.business.common.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.g1park.throwingmoney.business.common.constant.AccountType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@Data
public class Account {
    private String accountNo;
    private int ownerID;
    private int type;
    private long balance;
    private LocalDateTime insDate;
    private LocalDateTime updDate;

    public Account(int ownerID, int type, long balance){
        this.ownerID = ownerID;
        this.type = type;
        this.balance = balance;

        if(type == AccountType.SYSTEM.ConvertNumericCode()){
            String formatedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            this.accountNo = "S"+formatedDate+ownerID;
        }

    }

}
