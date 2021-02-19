package net.g1park.throwingmoney.business.model.constant;

public enum AccountType {
    USER(1),
    SYSTEM(2);

    private final int numericCode;
    AccountType(int numericCode){
        this.numericCode = numericCode;
    }

    public int ConvertNumericCode() { return numericCode;}
};