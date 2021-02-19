package net.g1park.throwingmoney.business.controller.responsemodel;


import lombok.Data;
import lombok.NoArgsConstructor;

import net.g1park.throwingmoney.business.common.constant.ResultCode;

@NoArgsConstructor
@Data
public class APIResponse {
    private int resultCode;
    private String message;
    private Object data;

    public APIResponse(int resultCode){
        this.resultCode = resultCode;
    }

    public APIResponse(ResultCode resultCode){
        this.resultCode = resultCode.ConvertNumericCode();
        this.message = resultCode.toString();   // 클라이언트 통신시에는 직접적인 오류내용은 나타내지 않도록 다시 손봐야..
    }

    public APIResponse(ResultCode resultCode, String message, Object data){
        this.resultCode = resultCode.ConvertNumericCode();
        this.message = message;
        this.data = data;
    }
}

