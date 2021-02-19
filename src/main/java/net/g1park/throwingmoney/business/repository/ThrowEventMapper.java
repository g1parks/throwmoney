package net.g1park.throwingmoney.business.repository;

import net.g1park.throwingmoney.business.common.model.ThrowEvent;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

@Mapper
@Service
public interface ThrowEventMapper {

    int UpdateThrowEventToken(ThrowEvent throwEvent);

    int InsertThrowEvent(ThrowEvent throwEvent);
    ThrowEvent SelectThrowEvent(int throwID);

}
