package net.g1park.throwingmoney.business.repository;

import net.g1park.throwingmoney.business.model.Transfer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransferMapper {
    int InsertTransfer(Transfer transfer);
    int UpdateTransfer(Transfer transfer);
}
