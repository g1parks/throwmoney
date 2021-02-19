package net.g1park.throwingmoney.business.service;

import net.g1park.throwingmoney.business.model.Account;
import net.g1park.throwingmoney.business.repository.AccountMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Test
    void getAccountInfo() {
        Account resultInfo = accountService.GetAccountInfo("01011112222");
        assertEquals("01011112222", resultInfo.getAccountNo());

    }

    @Test
    void getAccountInfoByOwnerID() {
    }

    @Test
    void modifyAccountBlanceByOwnerID() {
    }

    @Test
    void modifyAccountBlance() {
    }
}