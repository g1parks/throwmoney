package net.g1park.throwingmoney.business.controller;

import net.g1park.throwingmoney.business.common.model.MyReferenceDeliver;
import net.g1park.throwingmoney.business.common.model.ThrowEvent;
import net.g1park.throwingmoney.business.common.model.ThrowEventStatus;
import net.g1park.throwingmoney.business.common.model.ThrowTarget;
import net.g1park.throwingmoney.business.controller.responsemodel.APIResponse;
import net.g1park.throwingmoney.business.controller.responsemodel.ResponseGetThrowMoneyInfo;
import net.g1park.throwingmoney.business.controller.responsemodel.ResponsePickupThrownMoney;
import net.g1park.throwingmoney.business.controller.responsemodel.ResponseThrowMoney;
import net.g1park.throwingmoney.business.common.constant.ResultCode;
import net.g1park.throwingmoney.business.service.ThrowEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/throw")
public class ThrowController {

    @Autowired
    private ThrowEventService throwEventService;

    @PostMapping("RequestThrowMoney")
    public ResponseEntity<APIResponse> RequestThrowMoney(@RequestHeader(value="X-USER-ID") int userID,
                                                         @RequestHeader(value="X-ROOM-ID") String roomID,
                                                         @RequestBody Map<String, String> params) {
        long totalAmount;
        int totalCount;
        try{
             totalAmount = Long.parseLong(params.get("totalAmount"));
             totalCount = Integer.parseInt(params.get("targetCount"));
        }catch(NumberFormatException e)
        {
            return new ResponseEntity(new APIResponse(ResultCode.INVALID_PARAMETER_FORMAT), HttpStatus.OK);
        }

        if (userID <= 0)
            return new ResponseEntity(new APIResponse(ResultCode.USER_ID_IS_NOT_SUPPLIED), HttpStatus.OK);
        if (roomID.isEmpty())
            return new ResponseEntity(new APIResponse(ResultCode.ROOM_ID_IS_NOT_SUPPLIED), HttpStatus.OK);

        MyReferenceDeliver<ThrowEvent> eventRef = new MyReferenceDeliver();  // 아쉬운 순간...

        ResultCode resultCode = throwEventService.CreateThrowEvent(roomID, userID, totalAmount, totalCount, eventRef);

        if (resultCode != ResultCode.SUCCESS)
            return new ResponseEntity(new APIResponse(resultCode), HttpStatus.OK);

        ResponseThrowMoney response = new ResponseThrowMoney();
        response.setToken(eventRef.obj.getToken());
        response.setThrowID(eventRef.obj.getThrowID());

        return new ResponseEntity(new APIResponse(ResultCode.SUCCESS, "", response), HttpStatus.OK);
    }

    @PostMapping("RequestPickupMoney")
    public ResponseEntity<APIResponse> RequestPickupMoney(@RequestHeader(value="X-USER-ID") int userID,
                                                          @RequestHeader(value="X-ROOM-ID") String roomID,
                                                          @RequestBody Map<String, String> params){
        String token;
        int throwID;

        try{
            token = params.get("token");
            throwID = Integer.parseInt(params.get("throwID"));
        }catch(NumberFormatException e){
            return new ResponseEntity(new APIResponse(ResultCode.INVALID_PARAMETER_FORMAT), HttpStatus.OK);
        }

        if (userID <= 0)
            return new ResponseEntity(new APIResponse(ResultCode.USER_ID_IS_NOT_SUPPLIED), HttpStatus.OK);
        if (roomID.isEmpty())
            return new ResponseEntity(new APIResponse(ResultCode.ROOM_ID_IS_NOT_SUPPLIED), HttpStatus.OK);
        if (throwID<=0)
            return new ResponseEntity(new APIResponse(ResultCode.THROW_ID_IS_NOT_SUPPLIED), HttpStatus.OK);
        if (token.isEmpty())
            return new ResponseEntity(new APIResponse(ResultCode.TOKEN_IS_NOT_SUPPLIED), HttpStatus.OK);

        //to do : 기타 유효성 체크

        MyReferenceDeliver<ThrowTarget> targetRef = new MyReferenceDeliver();
        ResultCode resultCode = throwEventService.PickupThrownMoney(userID, throwID, roomID, token, targetRef);

        if (resultCode != ResultCode.SUCCESS)
            return new ResponseEntity(new APIResponse(resultCode), HttpStatus.OK);

        ResponsePickupThrownMoney response = new ResponsePickupThrownMoney();
        response.setGainedMoney(targetRef.obj.getDividedAmount());

        return new ResponseEntity(new APIResponse(ResultCode.SUCCESS, "", response), HttpStatus.OK);
    }

    // 뿌리기 현황 조회
    @PostMapping("GetThrowMoneyInfo")
    public ResponseEntity<APIResponse> GetThrowMoneyInfo(@RequestHeader(value="X-USER-ID") int userID,
                                                          @RequestHeader(value="X-ROOM-ID") String roomID,
                                                          @RequestBody Map<String, String> params){
        String token;
        int throwID;

        try{
            token = params.get("token");
            throwID = Integer.parseInt(params.get("throwID"));
        }catch(NumberFormatException e){
            return new ResponseEntity(new APIResponse(ResultCode.INVALID_PARAMETER_FORMAT), HttpStatus.OK);
        }

        if (userID <= 0)
            return new ResponseEntity(new APIResponse(ResultCode.USER_ID_IS_NOT_SUPPLIED), HttpStatus.OK);
        if (roomID.isEmpty())
            return new ResponseEntity(new APIResponse(ResultCode.ROOM_ID_IS_NOT_SUPPLIED), HttpStatus.OK);
        if (throwID<=0)
            return new ResponseEntity(new APIResponse(ResultCode.THROW_ID_IS_NOT_SUPPLIED), HttpStatus.OK);
        if (token.isEmpty())
            return new ResponseEntity(new APIResponse(ResultCode.TOKEN_IS_NOT_SUPPLIED), HttpStatus.OK);


        MyReferenceDeliver<ThrowEventStatus> statusRef = new MyReferenceDeliver();
        ResultCode resultCode = throwEventService.GetThrowMoneyInfo(userID, throwID, roomID, token, statusRef);

        if (resultCode != ResultCode.SUCCESS)
            return new ResponseEntity(new APIResponse(resultCode), HttpStatus.OK);



        ResponseGetThrowMoneyInfo response = new ResponseGetThrowMoneyInfo();
        {
            ArrayList<ThrowTarget> throwTargetList = statusRef.obj.getThrowTargetList();
            List<Integer> receiverIDList = new ArrayList();
            long sumOfPickedupMoney = 0;

            for (int i = 0; i < throwTargetList.size(); i++) {
                ThrowTarget tempTarget = throwTargetList.get(i);
                if (tempTarget.getReceiverID() > 0) {
                    receiverIDList.add(tempTarget.getReceiverID());
                    sumOfPickedupMoney+=tempTarget.getDividedAmount();
                }
            }

            response.setThrowEventTime(statusRef.obj.getInsDate());
            response.setAmountOfMoeny(statusRef.obj.getTotalAmount());
            response.setSumOfReceivedMoney(sumOfPickedupMoney);
            response.setReceiverIDList(receiverIDList);

        }



        return new ResponseEntity(
                new APIResponse(ResultCode.SUCCESS, ResultCode.SUCCESS.toString(), response), HttpStatus.OK);
    }
}
