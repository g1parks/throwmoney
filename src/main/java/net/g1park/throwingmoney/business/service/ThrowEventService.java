package net.g1park.throwingmoney.business.service;

import net.g1park.throwingmoney.business.common.model.*;
import net.g1park.throwingmoney.business.common.constant.AccountType;
import net.g1park.throwingmoney.business.common.constant.Common;
import net.g1park.throwingmoney.business.common.constant.ResultCode;
import net.g1park.throwingmoney.business.common.util.MyUtil;
import net.g1park.throwingmoney.business.repository.AccountMapper;
import net.g1park.throwingmoney.business.repository.RoomMapper;
import net.g1park.throwingmoney.business.repository.ThrowEventMapper;
import net.g1park.throwingmoney.business.repository.ThrowTargetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ThrowEventService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private ThrowEventMapper throwEventMapper;

    @Autowired
    private ThrowTargetMapper throwTargetMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private MyUtil myUtil;


    // 뿌리기 이벤트 생성
    @Transactional(rollbackFor = {Exception.class})
    public ResultCode CreateThrowEvent(String roomID,
                                       int ownerID,
                                       long totalAmount,
                                       int targetCount,
                                       MyReferenceDeliver<ThrowEvent> wrappedThrowEvent){

        try {
            Account ownerAccount = accountMapper.SelectAccountInfoByOwnerID(ownerID);
            if (ownerAccount == null)
                return ResultCode.INVALID_ACCOUNT;

            ThrowEvent newThrowEvent = new ThrowEvent(roomID, ownerID, totalAmount, targetCount);       // 이벤트 객체 먼저 생성
            throwEventMapper.InsertThrowEvent(newThrowEvent);

            Account newSystemAccount =
                    new Account(newThrowEvent.getThrowID(), AccountType.SYSTEM.ConvertNumericCode(), 0);// 일회성 계좌 생성
            accountMapper.InsertAccount(newSystemAccount);

            // 토큰 생성 및 뿌리기 이벤트 정보 저장
            {
                String timestamp = String.valueOf(newThrowEvent.getInsDate().getHour());                // 생성 시간을 salt로
                String token = myUtil.GenerateToken(String.valueOf(newThrowEvent.getThrowID()), timestamp);

                newThrowEvent.setToken(token);
                newThrowEvent.setAccountNo(newSystemAccount.getAccountNo());
                throwEventMapper.UpdateThrowEventToken(newThrowEvent);                                 // 토큰과 계좌번호 등록
            }

            // 받아갈 돈 얻어와 저장
            ArrayList<Long> dividedAmountList = myUtil.DivideNumberByRandomRatio(totalAmount, targetCount);
            for (int i = 0; i < dividedAmountList.size(); i++) {
                ThrowTarget target = new ThrowTarget(
                        newThrowEvent.getThrowID(),
                        -1,
                        dividedAmountList.get(i),
                        newThrowEvent.getToken(),
                        newThrowEvent.getInsDate()
                );
                throwTargetMapper.InsertThrowTarget(target);
            }

            // 주최자 -> 시스템계좌로 뿌린 금액 이체
            {
                newSystemAccount.setBalance(newSystemAccount.getBalance() + newThrowEvent.getTotalAmount());
                if (accountMapper.UpdateAccountBalance(newSystemAccount) <= 0)
                    return ResultCode.NO_AFFECTED_DATA;
                ownerAccount.setBalance(ownerAccount.getBalance() - newThrowEvent.getTotalAmount());
                if (accountMapper.UpdateAccountBalance(ownerAccount) <= 0)
                    return ResultCode.NO_AFFECTED_DATA;
            }

            wrappedThrowEvent.obj = newThrowEvent; // 생성된 뿌리기 이벤트 정보리턴

        } catch(Exception e) {
            // to do : logging
           return ResultCode.CANNOT_CREATE_EVENT;
        }

        return ResultCode.SUCCESS;
    }

    // 줍기 요청
    @Transactional(rollbackFor = {Exception.class}, isolation= Isolation.REPEATABLE_READ)
    public ResultCode PickupThrownMoney(
            int userID,
            int throwID,
            String roomID,
            String token,
            MyReferenceDeliver<ThrowTarget> wrappedThrowTarget){

        try {
            List<Integer> roomMemberList = roomMapper.SelectRoomMenberListByRoomID(roomID);

            if (roomMemberList == null)
                return ResultCode.ROOM_IS_NOT_EXIST;

            if (!roomMemberList.contains(userID))
                return ResultCode.INVALID_ROOM_MEMBER;                              // 클라가 넘겨준 방번호가 유효하지 않음

            ThrowEvent throwEvent = throwEventMapper.SelectThrowEvent(throwID);
            if (throwEvent == null)
                return ResultCode.THROWEVENT_IS_NOT_EXIST;                          // 왠일인지 이벤트가 존재하지 않

            if (!throwEvent.getRoomID().equals(roomID))
                return ResultCode.INVALID_ROOM_MEMBER;                              // 돈 뿌려진 방사람 아님

            // token 검증
            {
                String timestamp = String.valueOf(throwEvent.getInsDate().getHour());
                String remadedToken = myUtil.GenerateToken(String.valueOf(throwEvent.getThrowID()), timestamp);
                if (!remadedToken.equals(token))
                    return ResultCode.INVALID_TOKEN;                                // 토큰 불일치. 훼손된 토큰
            }

            if (LocalDateTime.now().isAfter(throwEvent.getInsDate().plusMinutes(Common.CUT_OFF_PICKUP_TIME_MIN)))
                return ResultCode.EXPIRATION_TIME_FOR_PICK;                         // 제한시간이 지나면 주울 수 없음

            if (throwEvent.getOwnerID() == userID)
                return ResultCode.CANNOT_PICKUP_SELF;                               // 자기껀 주울 수 없음

            ArrayList<ThrowTarget> unpickedThrowTargetList = new ArrayList();
            ArrayList<ThrowTarget> throwTargetList = throwTargetMapper.SelectThrowTargetList(throwEvent.getThrowID());
            for (int i = 0; i < throwTargetList.size(); i++) {
                ThrowTarget tempTarget = throwTargetList.get(i);
                if (tempTarget.getReceiverID() <= 0) {
                    unpickedThrowTargetList.add(tempTarget);                        // 아직 안주워간 돈은 따로 분류
                    continue;
                }
                if (throwTargetList.get(i).getReceiverID() == userID)
                    return ResultCode.ALREADY_GAINED_MONEY;                         // 받은 이력이 있으면 즉시 복귀
            }

            if (unpickedThrowTargetList.size() <= 0)
                return ResultCode.EVENT_HAS_FINISHED;                               // 전부 줍줍했으로 진행 취소

            int randomOffset = (new Random().nextInt(unpickedThrowTargetList.size() - 1));
            ThrowTarget finalTarget = unpickedThrowTargetList.get(randomOffset);    // 안주워간 돈꾸러미 중 하나 선택

            // 시스템 계좌-> 요청자 계좌로 이체
            {
                Account pickerAccount = accountMapper.SelectAccountInfoByOwnerID(userID);
                if (pickerAccount == null)
                    return ResultCode.INVALID_ACCOUNT;
                Account systemAccount = accountMapper.SelectAccountInfoByOwnerID(throwID);
                if (systemAccount == null)
                    return ResultCode.INVALID_ACCOUNT;

                pickerAccount.setBalance(pickerAccount.getBalance() + finalTarget.getDividedAmount());
                if (accountMapper.UpdateAccountBalance(pickerAccount) <= 0)
                    return ResultCode.NO_AFFECTED_DATA;
                systemAccount.setBalance(systemAccount.getBalance() - finalTarget.getDividedAmount());
                if (accountMapper.UpdateAccountBalance(systemAccount) <= 0)
                    return ResultCode.NO_AFFECTED_DATA;
            }

            // 돈 주웠음을 기록
            {
                finalTarget.setResultCode(ResultCode.SUCCESS.ConvertNumericCode());
                finalTarget.setReceiverID(userID);
                throwTargetMapper.UpdateThrowTarget(finalTarget);
            }

            wrappedThrowTarget.obj = finalTarget;                                     // 주운 돈 정보 리턴

        }catch (Exception e) {
            // to do : logging
            return ResultCode.CANNOT_PICKUP_MONEY;
        }

        return ResultCode.SUCCESS;
   }

    // 이벤트 내역 조회
    public ResultCode GetThrowMoneyInfo(int userID, int throwID, String roomID, String token, MyReferenceDeliver<ThrowEventStatus> wrappedThrowStatus){

        try {
            ThrowEvent throwEvent = throwEventMapper.SelectThrowEvent(throwID);

            if (throwEvent == null)
                return ResultCode.THROWEVENT_IS_NOT_EXIST;

            if (!throwEvent.getRoomID().equals(roomID))
                return ResultCode.INVALID_ROOM_MEMBER;                              // 뿌리기가 된 방에 소속되어있지 않음

            // token 검증
            {
                String timestamp = String.valueOf(throwEvent.getInsDate().getHour());
                String remadedToken = myUtil.GenerateToken(String.valueOf(throwEvent.getThrowID()), timestamp);
                if (!remadedToken.equals(token))
                    return ResultCode.INVALID_TOKEN;                                // 토큰 불일치. 훼손된 토큰
            }

            if (LocalDateTime.now().isAfter(throwEvent.getInsDate().plusDays(Common.EXPIRATION_DAY_FOR_READ)))
                return ResultCode.EXPIRATION_DATE_FOR_READ;                         // 제한시간이 지나면 조회가 불가

            if (throwEvent.getOwnerID() != userID)
                return ResultCode.PERMISSION_DENIED;                               // 이벤트 오너가 아니면 조회 불가

            ArrayList<ThrowTarget> throwTargetList = throwTargetMapper.SelectThrowTargetList(throwEvent.getThrowID());

            ThrowEventStatus eventStatus = new ThrowEventStatus(throwEvent);
            eventStatus.setThrowTargetList(throwTargetList);

            wrappedThrowStatus.obj = eventStatus;
        }catch (Exception e){

            return ResultCode.CANNOT_READ_EVENT_INFO;
        }
        return ResultCode.SUCCESS;
    }
}
