package net.g1park.throwingmoney.business.controller;

import net.g1park.throwingmoney.business.model.api.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.g1park.throwingmoney.business.service.AccountService;
import net.g1park.throwingmoney.business.model.constant.ResultCode;
import net.g1park.throwingmoney.business.model.Account;

@RestController
@RequestMapping("/transfer")
public class TransferController {

    @Autowired
    private AccountService service;

    @GetMapping("ThrowMoney")
    public ResponseEntity<APIResponse> RequestThrowMoney(){

        String tempData = "temp temp temp";
        APIResponse response = new APIResponse();
        response.setData(tempData);
        return new ResponseEntity<APIResponse>(response, HttpStatus.OK);
    }

    @GetMapping("GetAccountInfoByUserID")
    public ResponseEntity<APIResponse> GetAccountInfoByUserID(int userID){

        if(userID<=0)
            return new ResponseEntity<APIResponse>(new APIResponse(ResultCode.USER_ID_IS_NOT_SUPPLIED), HttpStatus.OK);
        Account accountInfo = service.GetAccountInfoByOwnerID(userID);

        if(accountInfo == null)
            return new ResponseEntity<APIResponse>(new APIResponse(ResultCode.NO_MATCHED_DATA), HttpStatus.OK);

        APIResponse response = new APIResponse();
        response.setData(accountInfo);

        return new ResponseEntity<APIResponse>(response, HttpStatus.OK);

    }

    @GetMapping("GetAccountInfo")
    public ResponseEntity<APIResponse> GetAccountInfo(String accountNo){

        if(accountNo.isEmpty())
            return new ResponseEntity<APIResponse>(new APIResponse(ResultCode.USER_ID_IS_NOT_SUPPLIED), HttpStatus.OK);
        Account accountInfo = service.GetAccountInfo(accountNo);

        if(accountInfo == null){
            return new ResponseEntity<APIResponse>(new APIResponse(ResultCode.NO_MATCHED_DATA), HttpStatus.OK);
        }
        APIResponse response = new APIResponse();
        response.setData(accountInfo);

        return new ResponseEntity<APIResponse>(response, HttpStatus.OK);

    }


}
