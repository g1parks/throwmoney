package net.g1park.throwingmoney.business.model.constant;

// 공용 결과 코드 값이며, 클라이언트에게는 숫자코드만 보내거나, 한번 걸러서 상세 정보가 넘어가지 않도록 다듬어야 함..시간 부족..
public enum ResultCode {
    SUCCESS(0),
    FAILED(100),
    NULL_POINTER(110),
    INVALID_PARAMETER_FORMAT(111),
    USER_ID_IS_NOT_SUPPLIED(120),
    ROOM_ID_IS_NOT_SUPPLIED(121),
    THROW_ID_IS_NOT_SUPPLIED(122),
    TOKEN_IS_NOT_SUPPLIED(123),
    TIME_OUT(130),
    NO_MATCHED_DATA(140),
    INVALID_ACCOUNT(150),
    CANNOT_UPDATE_TOKEN(160),
    INVALID_ROOM_ID(170),
    THROWEVENT_IS_NOT_EXIST(180),
    ROOM_IS_NOT_EXIST(181),
    INVALID_ROOM_MEMBER(190),
    INVALID_TOKEN(200),
    EXPIRATION_TIME_FOR_PICK(210),
    EXPIRATION_DATE_FOR_READ(211),
    CANNOT_PICKUP_SELF(220),
    EVENT_HAS_FINISHED(230),
    NO_AFFECTED_DATA(240),
    ALREADY_GAINED_MONEY(250),
    PERMISSION_DENIED(260),
    CANNOT_CREATE_ACCOUNT(270);

    private final int numericCode;

    ResultCode(int numericCode){
        this.numericCode = numericCode;
    }

    public int ConvertNumericCode() { return numericCode;}

}
