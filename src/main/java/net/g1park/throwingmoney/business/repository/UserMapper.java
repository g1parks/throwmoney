package net.g1park.throwingmoney.business.repository;

import org.apache.ibatis.annotations.Mapper;
import net.g1park.throwingmoney.business.common.model.User;

@Mapper
public interface UserMapper {
    //int InsertUser(User user);
    //int UpdateUser(User User);

    User SelectUserInfoByUserID(String userID);
}
