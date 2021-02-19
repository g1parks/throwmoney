package net.g1park.throwingmoney.business.repository;

import org.apache.ibatis.annotations.Mapper;
import net.g1park.throwingmoney.business.common.model.Room;
import org.springframework.stereotype.Service;

import java.util.List;

@Mapper
@Service
public interface RoomMapper {

    Room SelectRoomInfoByOwnerID(int ownerID);
    Room SelectRoomInfoByRoomID(String roomID);
    List<Integer> SelectRoomMenberListByRoomID(String roomID);
}
