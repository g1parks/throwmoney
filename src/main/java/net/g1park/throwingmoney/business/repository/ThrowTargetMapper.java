package net.g1park.throwingmoney.business.repository;

import net.g1park.throwingmoney.business.model.ThrowEvent;
import net.g1park.throwingmoney.business.model.ThrowTarget;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Mapper
@Service
public interface ThrowTargetMapper {

    int InsertThrowTarget(ThrowTarget throwTarget);
    ArrayList<ThrowTarget> SelectThrowTargetList(int throwID);
    ArrayList<ThrowTarget> SelectUnpickedThrowTargetList(int throwID);
    ThrowTarget SelectThrowTarget(long seqNo);
    int UpdateThrowTarget(ThrowTarget throwTarget);


}