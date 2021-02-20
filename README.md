# 카카오페이 뿌리기 기능 구현하기

개발환경

- MacOS / IntelliJ 2018
- Spring Boot (Java SDK 8)
- MySQL 5 (Google cloude) / MyBatis 3
- Swagger (http://localhost:8080/swagger-ui.html)

## API Spec
#### 헤더 공통
- X-USER-ID
- X-ROOM-ID
- - -
  
#### 돈 뿌리기 (POST) /throw/RequestThrowMoney
request sample
<pre><code>
{
"totalAmount":10000, // 뿌릴 돈
"targetCount":3 // 받을 사람 수
}
</code></pre>
response sample
<pre><code>
{
  "resultCode": 0,
  "message": "SUCCESS",
  "data": {
    "token": "ec1", // 뿌리기 검증 토큰(암호화키)
    "throwID": 65 // 뿌리기 식별 키(공개키)
  }
}
</code></pre>
- - -
#### 돈 줍기 (POST) /throw/RequestPickupMoney
request sample
<pre><code>
{
"throwID": 65, // 뿌리기 식별 키
"token": "ec1" // 뿌리기 검증 토큰
}
</code></pre>
response sample
<pre><code>
{
  "resultCode": 0,
  "message": "SUCCESS",
  "data": {
    "gainedMoney": 2877 // 줍기 성공한 금액
  }
}
</code></pre>
- - -
#### 돈뿌리기 현황 조회 (POST) /throw/GetThrowMoneyInfo
request sample
<pre><code>
{
"throwID":66,
"token":"81c"
}
</code></pre>
response sample
<pre><code>
{
  "resultCode": 0,
  "message": "SUCCESS",
  "data": {
    "throwEventTime": "2021-02-20T07:54:54",
    "amountOfMoeny": 10000,
    "sumOfReceivedMoney": 4307,
    "receiverIDList": [
      10000002,
      10000003
    ]
  }
}
</code></pre>

## 구성

## 문제 해결 전략
줍기 요청에 대한 유효성 체크 (token의 중복가능성 제거)
- token 은 abusing을 막기 위해 반드시 중복되지 않아야 하나, 자동증가 개념이 들어갈 경우 예측이 가능해지고 무작위 해시코드를 만들더라도 세자리만 추출하여 사용할 경우 중복될 확률이 존재
- 일정 시간동안 발급된 토큰을 관리하는 비용보다, 유일성을 보장하는 키를 추가하여 쌍으로 운용하는 것이 수월하다고 판단

뿌리는 돈을 어떻게 나눌 것인가?
- 단순히 랜덤으로 처리할 경우 극단적인 결과가 나올 수 있어 서비스가 고객에게 주는 흥미도를 떨어뜨릴 것으로 판단
- 누군가가 너무 심하게 적거나, 너무 많지 않도록 금액의 분포가 중간 구간에 모여있도록 처리

여러개의 서버에서 구동될 경우 데이터 정합성 보장과 성능
- (부족한 개발시간으로) 세밀하게 고민할 수가 없어 트랜잭션 중첩이 아예 없도록 일차원적으로 구현하였음
- 줍기의 경우는 일련의 과정에 데이터가 변동되어서는 안되므로 격리수준을 높임.
- 반대로 조회의 경우는 트랜잭션을 아예 적용하지 않아도 무방하다고 판단하였음
- 부득이하게 mySQL 을 사용하였으나 Mongo로 교체하면 대규모 트래픽도 충분히 감당할 것으로 예상



- 
- 