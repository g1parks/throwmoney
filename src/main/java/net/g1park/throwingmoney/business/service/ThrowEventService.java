package net.g1park.throwingmoney.business.service;

import net.g1park.throwingmoney.business.model.*;
import net.g1park.throwingmoney.business.model.constant.AccountType;
import net.g1park.throwingmoney.business.model.constant.Common;
import net.g1park.throwingmoney.business.model.constant.ResultCode;
import net.g1park.throwingmoney.business.repository.AccountMapper;
import net.g1park.throwingmoney.business.repository.RoomMapper;
import net.g1park.throwingmoney.business.repository.ThrowEventMapper;
import net.g1park.throwingmoney.business.repository.ThrowTargetMapper;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;

import javax.xml.transform.Result;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Service
public class ThrowEventService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private ThrowEventMapper throwEventMapper;

    @Autowired
    private ThrowTargetMapper throwTargetMapper;

    @Autowired
    private RoomMapper roomMapper;

    // 토큰 생성. 간편한 MD5
    public String GenerateToken(String originKey, String saltKey){
        String encrytedOwnerID = DigestUtils.md5DigestAsHex(originKey.getBytes());
        String encrytedToken = DigestUtils.md5DigestAsHex( (encrytedOwnerID+saltKey).getBytes());
        return encrytedToken.substring(encrytedToken.length()-3, encrytedToken.length());
    }

    /*public List<int> RandomRatioList(int numberOfRatio){


    }*/

    // 뿌리기 이벤트 생성
    @Transactional
    public ResultCode CreateThrowEvent(String roomID, int ownerID, long totalAmount, int targetCount, MyReferenceDeliver<ThrowEvent> wrappedThrowEvent){

        Account ownerAccount = accountMapper.SelectAccountInfoByOwnerID(ownerID);
        if(ownerAccount == null)
            return ResultCode.INVALID_ACCOUNT;

        ThrowEvent newThrowEvent = new ThrowEvent(roomID, ownerID, totalAmount, targetCount);   // 이벤트 객체 먼저 생성
        if(throwEventMapper.InsertThrowEvent(newThrowEvent) <= 0)
            return ResultCode.FAILED;

        Account newSystemAccount = new Account(newThrowEvent.getThrowID(), AccountType.SYSTEM.ConvertNumericCode(), 0);
        if(accountMapper.InsertAccount(newSystemAccount) <=0)
            return ResultCode.CANNOT_CREATE_ACCOUNT;

        // 토큰 생성 및 뿌리기 이벤트 정보 저장
        {
            String timestamp = String.valueOf(newThrowEvent.getInsDate().getHour());   // 생성 시간을 salt
            String token = GenerateToken(String.valueOf(newThrowEvent.getThrowID()), timestamp );

            newThrowEvent.setToken(token);
            newThrowEvent.setAccountNo(newSystemAccount.getAccountNo());
            int affectedRowCount = throwEventMapper.UpdateThrowEventToken(newThrowEvent);
            if(affectedRowCount <= 0)
                return ResultCode.CANNOT_UPDATE_TOKEN; // 이미 들어간 throwEvent는 DB 에 남아있게 됨. 시간남으면 처리
        }

        // 나누어 받아갈 돈 저장
        {
            long dividedAmount = totalAmount / targetCount;   // 기본 몫
            int remainder = (int) (totalAmount % targetCount);  // 나머지

            for (int i = 0; i < targetCount; i++) {              // 나머지를 1씩 소진될 때까지 분배
                int remainderBonus;
                if(remainder > 0) {
                    remainderBonus = 1;
                    remainder--;
                }else{
                    remainderBonus = 0;
                }

                ThrowTarget target = new ThrowTarget(newThrowEvent.getThrowID(),
                        -1,
                        dividedAmount + remainderBonus,
                        newThrowEvent.getToken(),
                        newThrowEvent.getInsDate()
                        );


                int createdSeqNo = throwTargetMapper.InsertThrowTarget(target);
                if (createdSeqNo <= 0)
                    return ResultCode.FAILED; // 이미 들어간 target 이 DB에 남아있게 됨, 시간남으면 처리

            }
        }

        // 주최자 -> 시스템계좌로 이체
        {
            newSystemAccount.setBalance(newSystemAccount.getBalance()+newThrowEvent.getTotalAmount());
            if(accountMapper.UpdateAccountBalance(newSystemAccount) <= 0)
                return ResultCode.NO_AFFECTED_DATA;
            ownerAccount.setBalance(ownerAccount.getBalance()-newThrowEvent.getTotalAmount());
            if(accountMapper.UpdateAccountBalance(ownerAccount) <= 0)
                return ResultCode.NO_AFFECTED_DATA;
        }

        wrappedThrowEvent.obj = newThrowEvent; // 생성된 뿌리기 이벤트 정보 (참조)리턴
        return ResultCode.SUCCESS;
    }

    // 줍기 요청
    @Transactional
    public ResultCode PickupThrownMoney(int userID, int throwID, String roomID, String token, MyReferenceDeliver<ThrowTarget> wrappedThrowTarget){

        List<Integer> roomMemberList = roomMapper.SelectRoomMenberListByRoomID(roomID);

        if(roomMemberList == null)
            return ResultCode.ROOM_IS_NOT_EXIST;

        if( !roomMemberList.contains(new Integer(userID)) )
            return ResultCode.INVALID_ROOM_MEMBER;                              // 클라가 넘겨준 방번호가 유효하지 않음

        ThrowEvent throwEvent = throwEventMapper.SelectThrowEvent(throwID);

        if(throwEvent == null)
            return ResultCode.THROWEVENT_IS_NOT_EXIST;

        if(!throwEvent.getRoomID().equals(roomID))
            return ResultCode.INVALID_ROOM_MEMBER;                              // 뿌리기가 된 방에 소속되어있지 않음

        // token 검증
        {
            String timestamp = String.valueOf(throwEvent.getInsDate().getHour());
            String remadedToken = GenerateToken(String.valueOf(throwEvent.getThrowID()), timestamp);
            if(!remadedToken.equals(token))
                return ResultCode.INVALID_TOKEN;                                // 토큰 불일치. 훼손된 토큰
        }

        if(LocalDateTime.now().isAfter(throwEvent.getInsDate().plusMinutes(Common.CUT_OFF_PICKUP_TIME_MIN)) )
            return ResultCode.EXPIRATION_TIME_FOR_PICK;                         // 제한시간이 지나면 주울 수 없음

        if(throwEvent.getOwnerID() == userID)
            return ResultCode.CANNOT_PICKUP_SELF;                               // 자기껀 주울 수 없음

        ArrayList<ThrowTarget> unpickedThrowTargetList = new ArrayList();
        ArrayList<ThrowTarget> throwTargetList = throwTargetMapper.SelectThrowTargetList(throwEvent.getThrowID());
        for(int i=0; i<throwTargetList.size() ; i++){
            ThrowTarget tempTarget = throwTargetList.get(i);
            if(tempTarget.getReceiverID()<= 0) {
                unpickedThrowTargetList.add(tempTarget);                        // 아직 안주워간 돈은 따로 분류
                continue;
            }
            if(throwTargetList.get(i).getReceiverID()== userID)
                return ResultCode.ALREADY_GAINED_MONEY;                         // 받은 이력이 있으면 진행 취소

        }

        if(unpickedThrowTargetList.size() <= 0)
            return ResultCode.EVENT_HAS_FINISHED;                               // 전부 줍줍했으로 진행 취소

        int randomOffset = (new Random().nextInt(unpickedThrowTargetList.size()-1));
        ThrowTarget finalTarget = unpickedThrowTargetList.get(randomOffset);

        if(throwEvent == null)
            return ResultCode.NULL_POINTER;                                     // 뭔가 비정상


        // 시스템 계좌-> 요청자 계좌로 이체
        {
            Account pickerAccount = accountMapper.SelectAccountInfoByOwnerID(userID);
            if(pickerAccount == null)
                return ResultCode.INVALID_ACCOUNT;
            Account systemAccount = accountMapper.SelectAccountInfoByOwnerID(throwID);
            if(systemAccount == null)
                return ResultCode.INVALID_ACCOUNT;

            pickerAccount.setBalance(systemAccount.getBalance()+finalTarget.getDividedAmount());
            if(accountMapper.UpdateAccountBalance(pickerAccount) <= 0)
                return ResultCode.NO_AFFECTED_DATA;
            systemAccount.setBalance(systemAccount.getBalance()-finalTarget.getDividedAmount());
            if(accountMapper.UpdateAccountBalance(systemAccount) <= 0)
                return ResultCode.NO_AFFECTED_DATA;
        }

        // 돈받았음을 기록
        {
            finalTarget.setResultCode(ResultCode.SUCCESS.ConvertNumericCode());
            finalTarget.setReceiverID(userID);
            int affectedRowCount = throwTargetMapper.UpdateThrowTarget(finalTarget);
            if (affectedRowCount <= 0)
                return ResultCode.NO_AFFECTED_DATA;                                 // 비정상. 트랜잭션 롤백 필요
        }

        wrappedThrowTarget.obj = finalTarget;                                     // 주운 돈 정보 리턴
        return ResultCode.SUCCESS;
   }

    // 이벤트 내역 조회
    @Transactional
    public ResultCode GetThrowMoneyInfo(int userID, int throwID, String roomID, String token, MyReferenceDeliver<ThrowEventStatus> wrappedThrowStatus){

        ThrowEvent throwEvent = throwEventMapper.SelectThrowEvent(throwID);

        if(throwEvent == null)
            return ResultCode.THROWEVENT_IS_NOT_EXIST;

        if(!throwEvent.getRoomID().equals(roomID))
            return ResultCode.INVALID_ROOM_MEMBER;                              // 뿌리기가 된 방에 소속되어있지 않음

        // token 검증
        {
            String timestamp = String.valueOf(throwEvent.getInsDate().getHour());
            String remadedToken = GenerateToken(String.valueOf(throwEvent.getThrowID()), timestamp);
            if(!remadedToken.equals(token))
                return ResultCode.INVALID_TOKEN;                                // 토큰 불일치. 훼손된 토큰
        }

        if(LocalDateTime.now().isAfter(throwEvent.getInsDate().plusDays(Common.EXPIRATION_DAY_FOR_READ)) )
            return ResultCode.EXPIRATION_DATE_FOR_READ;                                  // 제한시간이 지나면 조회가 불가

        if(throwEvent.getOwnerID() != userID)
            return ResultCode.PERMISSION_DENIED;                               // 이벤트 오너가 아니면 조회 불가

        ArrayList<ThrowTarget> throwTargetList = throwTargetMapper.SelectThrowTargetList(throwEvent.getThrowID());

        if(throwTargetList == null || throwTargetList.size()<=0)
            return ResultCode.NULL_POINTER;                                     // 뭔가 비정상

        ThrowEventStatus status = new ThrowEventStatus(throwEvent);
        status.setThrowTargetList(throwTargetList);

        wrappedThrowStatus.obj = status;

        return ResultCode.SUCCESS;
    }
}
