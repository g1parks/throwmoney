package net.g1park.throwingmoney.business.service;

import net.g1park.throwingmoney.business.common.constant.AccountType;
import net.g1park.throwingmoney.business.common.constant.ResultCode;
import net.g1park.throwingmoney.business.common.model.*;
import net.g1park.throwingmoney.business.common.util.MyUtil;
import net.g1park.throwingmoney.business.repository.AccountMapper;
import net.g1park.throwingmoney.business.repository.ThrowEventMapper;
import net.g1park.throwingmoney.business.repository.ThrowTargetMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.xml.transform.Result;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ThrowEventServiceTest {

    @Autowired
    ThrowEventService throwEventService;

    @Autowired
    ThrowEventMapper throwEventMapper;

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    MyUtil myUtil;

    @Autowired
    ThrowTargetMapper targetMapper;

    int eventOwnerID = 10000001;

    String roomID = "R1234";
    long eventMoney = 10000;
    int divisor = 3;
    static int eventID;
    static String eventToken;

    int user02 = 10000002;
    int user03 = 10000003;
    int user04 = 10000004;
    int outOfRoomUser = 10000005;


    @Test
    @Order(1)
    @DisplayName("돈뿌리기-성공 및 전후 주최자의 잔고 비교")
    void ThrowMoneyTest() {
        MyReferenceDeliver<ThrowEvent> tempEvent = new MyReferenceDeliver();

        long usersBeforeBalance = (accountMapper.SelectAccountInfoByOwnerID(eventOwnerID)).getBalance();

        ResultCode resultCode = throwEventService.CreateThrowEvent(roomID, eventOwnerID, eventMoney, divisor, tempEvent);

        long usersAfterBalance = (accountMapper.SelectAccountInfoByOwnerID(eventOwnerID)).getBalance();
        eventID = tempEvent.obj.getThrowID();
        eventToken = tempEvent.obj.getToken();

        assertEquals(ResultCode.SUCCESS, resultCode);
        assertEquals(usersBeforeBalance - eventMoney, usersAfterBalance);

    }

    @Test
    @Order(2)
    @DisplayName("돈줍기-비정상토큰이면 실패")
    void PickupMoneyTestWithWrongToken() {
        MyReferenceDeliver<ThrowTarget> tempTarget = new MyReferenceDeliver();
        ResultCode resultCode = throwEventService.PickupThrownMoney(user02, eventID, roomID, "121", tempTarget);

        assertEquals(ResultCode.INVALID_TOKEN, resultCode);
    }

    @Test
    @Order(3)
    @DisplayName("돈줍기-다른방사이람이면 실패")
    void PickupMoneyTestByOutOfRoomUser() {
        MyReferenceDeliver<ThrowTarget> tempTarget = new MyReferenceDeliver();
        ResultCode resultCode = throwEventService.PickupThrownMoney(outOfRoomUser, eventID, roomID, eventToken, tempTarget);

        assertEquals(ResultCode.INVALID_ROOM_MEMBER, resultCode);
    }

    @Test
    @Order(4)
    @DisplayName("돈줍기-자기자신은 실패")
    void PickupMoneyTestByOneSelf() {
        MyReferenceDeliver<ThrowTarget> tempTarget = new MyReferenceDeliver();
        ResultCode resultCode = throwEventService.PickupThrownMoney(eventOwnerID, eventID, roomID, eventToken, tempTarget);

        assertEquals(ResultCode.CANNOT_PICKUP_SELF, resultCode);
    }

    @Test
    @Order(5)
    @DisplayName("돈줍기-성공 및 주운 사람의 전후 잔고 비교")
    void PickupMoneyTest() {
        MyReferenceDeliver<ThrowTarget> tempTarget = new MyReferenceDeliver();

        long receiversBeforeBalance = (accountMapper.SelectAccountInfoByOwnerID(user02)).getBalance();
        ResultCode resultCode = throwEventService.PickupThrownMoney(user02, eventID, roomID, eventToken, tempTarget);
        long receiversAfterBalance = (accountMapper.SelectAccountInfoByOwnerID(user02)).getBalance();

        assertEquals(ResultCode.SUCCESS, resultCode);
        assertEquals(receiversBeforeBalance + tempTarget.obj.getDividedAmount(), receiversAfterBalance);
    }

    @Test
    @Order(6)
    @DisplayName("돈줍기-받고 또 시도하면 실패")
    void PickupMoneyTestRetry() {
        MyReferenceDeliver<ThrowTarget> tempTarget = new MyReferenceDeliver();
        ResultCode resultCode = throwEventService.PickupThrownMoney(user02, eventID, roomID, eventToken, tempTarget);

        assertEquals(ResultCode.ALREADY_GAINED_MONEY, resultCode);
    }

    @Test
    @Order(7)
    @DisplayName("돈줍기-제한시간초과면 실패")
    void PickupMoneyTest15MinLater() {
        int tempThrowEventID;
        String tempToken;

        {
            ThrowEvent newThrowEvent = new ThrowEvent(roomID, eventOwnerID, eventMoney, divisor);
            newThrowEvent.setInsDate(LocalDateTime.now().minusMinutes(15));
            throwEventMapper.InsertThrowEvent(newThrowEvent);
            tempThrowEventID = newThrowEvent.getThrowID();

            Account newSystemAccount =
                    new Account(newThrowEvent.getThrowID(), AccountType.SYSTEM.ConvertNumericCode(), 0);
            accountMapper.InsertAccount(newSystemAccount);

            String timestamp = String.valueOf(newThrowEvent.getInsDate().getHour());
            String token = myUtil.GenerateToken(String.valueOf(newThrowEvent.getThrowID()), timestamp);
            tempToken = token;

            newThrowEvent.setToken(token);
            newThrowEvent.setAccountNo(newSystemAccount.getAccountNo());
            throwEventMapper.UpdateThrowEventToken(newThrowEvent);

            ArrayList<Long> dividedAmountList = myUtil.DivideNumberByRandomRatio(eventMoney, divisor);
            for (int i = 0; i < dividedAmountList.size(); i++) {
                ThrowTarget target = new ThrowTarget(
                        newThrowEvent.getThrowID(),
                        -1,
                        dividedAmountList.get(i),
                        newThrowEvent.getToken(),
                        newThrowEvent.getInsDate()
                );
                targetMapper.InsertThrowTarget(target);
            }
        }

        MyReferenceDeliver<ThrowTarget> tempTarget = new MyReferenceDeliver();
        ResultCode resultCode = throwEventService.PickupThrownMoney(user02, tempThrowEventID, roomID, tempToken, tempTarget);

        assertEquals(ResultCode.EXPIRATION_TIME_FOR_PICK, resultCode);
    }

    @Test
    @Order(8)
    @DisplayName("조회하기-뿌린사람아니면 불가")
    void GetThrowMoneyInfoByOthers() {
        MyReferenceDeliver<ThrowEventStatus> tempStatus = new MyReferenceDeliver();
        ResultCode resultCode = throwEventService.GetThrowMoneyInfo(user02, eventID, roomID, eventToken, tempStatus);

        assertEquals(ResultCode.PERMISSION_DENIED, resultCode);

    }

    @Test
    @Order(9)
    @DisplayName("조회하기-7일 지나면 불가")
    void GetThrowMoneyInfo7DayLater() {
        int tempThrowEventID;
        String tempToken;

        {
            ThrowEvent newThrowEvent = new ThrowEvent(roomID, eventOwnerID, eventMoney, divisor);
            newThrowEvent.setInsDate(LocalDateTime.now().minusDays(7));
            throwEventMapper.InsertThrowEvent(newThrowEvent);
            tempThrowEventID = newThrowEvent.getThrowID();

            Account newSystemAccount =
                    new Account(newThrowEvent.getThrowID(), AccountType.SYSTEM.ConvertNumericCode(), 0);
            accountMapper.InsertAccount(newSystemAccount);

            String timestamp = String.valueOf(newThrowEvent.getInsDate().getHour());
            String token = myUtil.GenerateToken(String.valueOf(newThrowEvent.getThrowID()), timestamp);
            tempToken = token;

            newThrowEvent.setToken(token);
            newThrowEvent.setAccountNo(newSystemAccount.getAccountNo());
            throwEventMapper.UpdateThrowEventToken(newThrowEvent);

            ArrayList<Long> dividedAmountList = myUtil.DivideNumberByRandomRatio(eventMoney, divisor);
            for (int i = 0; i < dividedAmountList.size(); i++) {
                ThrowTarget target = new ThrowTarget(
                        newThrowEvent.getThrowID(),
                        -1,
                        dividedAmountList.get(i),
                        newThrowEvent.getToken(),
                        newThrowEvent.getInsDate()
                );
                targetMapper.InsertThrowTarget(target);
            }
        }
        MyReferenceDeliver<ThrowEventStatus> tempStatus = new MyReferenceDeliver();
        ResultCode resultCode = throwEventService.GetThrowMoneyInfo(eventOwnerID, tempThrowEventID, roomID, tempToken, tempStatus);

        assertEquals(ResultCode.EXPIRATION_DATE_FOR_READ, resultCode);
    }


}