package net.g1park.throwingmoney.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.g1park.throwingmoney.business.model.Account;
import net.g1park.throwingmoney.business.repository.AccountMapper;

@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;

    public Account GetAccountInfo(String accountNo){
        Account accountInfo = accountMapper.SelectAccountInfo(accountNo);

        return accountInfo;
    }

    public Account GetAccountInfoByOwnerID(int ownerID){
        Account accountInfo = accountMapper.SelectAccountInfoByOwnerID(ownerID);
        return accountInfo;
    }


}
