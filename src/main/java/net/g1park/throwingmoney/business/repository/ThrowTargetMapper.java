package net.g1park.throwingmoney.business.repository;

import net.g1park.throwingmoney.business.common.model.ThrowTarget;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Mapper
@Service
public interface ThrowTargetMapper {

    int InsertThrowTarget(ThrowTarget throwTarget);
    ArrayList<ThrowTarget> SelectThrowTargetList(int throwID);
    ThrowTarget SelectThrowTarget(long seqNo);
    int UpdateThrowTarget(ThrowTarget throwTarget);


}