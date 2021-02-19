package net.g1park.throwingmoney.business.repository;

import org.apache.ibatis.annotations.Mapper;
import net.g1park.throwingmoney.business.model.Account;
import org.springframework.stereotype.Service;

@Mapper
@Service
public interface AccountMapper {

    int InsertAccount(Account account);
    int UpdateAccountBalance(Account account);

    public Account SelectAccountInfoByOwnerID(int ownerID);
    public Account SelectAccountInfo(String accountNo);

}
