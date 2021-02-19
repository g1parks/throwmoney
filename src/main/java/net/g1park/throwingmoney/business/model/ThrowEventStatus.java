package net.g1park.throwingmoney.business.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class ThrowEventStatus extends ThrowEvent{

    private ArrayList<ThrowTarget> throwTargetList;

    public ThrowEventStatus(ThrowEvent event){
        super(event.getThrowID(),
        event.getRoomID(),
        event.getOwnerID(),
        event.getTotalAmount(),
        event.getTargetCount(),
        event.getToken(),
        event.getAccountNo(),
        event.getInsDate(),
        event.getUpdDate());
    }

}
