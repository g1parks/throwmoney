package net.g1park.throwingmoney.business.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;


import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransferControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void requestThrowMoney() throws Exception{
        mvc.perform(get("/RequestThrowMoney"))
                .andExpect(status().isOk());
    }

    @Test
    void getAccountInfoByUserID() throws Exception{

        String userID = "10000005";


        mvc.perform(
                get("/GetAccountInfoByUserID")
                    .param("userID", userID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNo").value("011045671234"));
    }

}