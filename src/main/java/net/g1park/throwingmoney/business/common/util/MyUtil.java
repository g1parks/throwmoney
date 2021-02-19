package net.g1park.throwingmoney.business.common.util;

import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.*;

@Component
public class MyUtil {
    // 토큰 생성.
    // MD5(MD5(이벤트 번호) + 현재시간)
    public String GenerateToken(String originKey, String saltKey){
        String encrytedOwnerID = DigestUtils.md5DigestAsHex(originKey.getBytes());
        String encrytedToken = DigestUtils.md5DigestAsHex( (encrytedOwnerID+saltKey).getBytes());
        return encrytedToken.substring(encrytedToken.length()-3, encrytedToken.length());
    }

    // originNumber 는 충분히 크다고 가정
    // 몰아주거나 독박쓰지 않도록(너무 작은 값도 너무 큰 값도 나오지 않도록) 중간값에 모이는 것을 의도
    public ArrayList<Long> DivideNumberByRandomRatio(long originNumber, int divisor){

        ArrayList<Long> result = new ArrayList();
        if(divisor <=1) {
            result.add(originNumber);
            return result;
        }

        long firstNumber = Math.abs(new Random().nextLong())%(originNumber-(originNumber*10/100) );
        long secondNumber = originNumber - firstNumber;

        if (divisor == 2) {
            result.add(firstNumber);
            result.add(secondNumber);
            return result;
        }

        TreeMap<Long, Long> treeMap = new TreeMap<Long, Long>();
        treeMap.put(firstNumber, firstNumber);
        treeMap.put(secondNumber, secondNumber);

        while(treeMap.size() <= divisor) {
            long target = treeMap.lastKey();
            long first = Math.abs(new Random().nextLong())%(target - (target * 10 / 100));
            long second = target - first;
            treeMap.remove(target);
            treeMap.put(first, first);
            treeMap.put(second, second);
        }

        result.addAll(treeMap.values());
        return result;
    }
}
