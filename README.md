# 카카오페이 뿌리기 기능 구현하기

개발환경

- MacOS / IntelliJ 2018
- Spring Boot (Java SDK 8)
- MySQL(Google cloud사용, public으로 열려있는상태) / MyBatis
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
"throwID":66, // 뿌리기 이벤트 식별키
"token":"81c" // 검증 토큰
}
</code></pre>
response sample
<pre><code>
{
  "resultCode": 0,
  "message": "SUCCESS",
  "data": {
    "throwEventTime": "2021-02-20T07:54:54", // 뿌린 시간
    "amountOfMoeny": 10000, // 뿌린 돈
    "sumOfReceivedMoney": 4307, // 현재 주워간 돈 합계
    "receiverIDList": [ // 주워간 사람 ID
      10000002,
      10000003
    ]
  }
}
</code></pre>

## 구성
![simple archi](https://user-images.githubusercontent.com/18466360/108577297-e1ab7980-7363-11eb-89e6-e1decfab42c2.png)


## 문제 해결 전략
줍기 요청에 대한 유효성 체크 (token의 중복가능성 제거)
- token 은 abusing을 막기 위해 반드시 중복되지 않아야 하나, 자동증가 개념이 들어갈 경우 예측이 가능해지고 무작위 해시코드를 만들더라도 세자리만 추출하여 사용할 경우 중복될 확률이 존재
- 일정 시간동안 발급된 토큰을 관리하는 비용보다, 유일성을 보장하는 키를 추가하여 쌍으로 운용하는 것이 수월하다고 판단
- 간단히 이벤트ID에 시간을 넣어 해싱하고, 검증시에도 동일한방법으로 해싱하여 비교

뿌리는 돈을 어떻게 나눌 것인가?
- 단순히 랜덤으로 처리할 경우 극단적인 결과가 나올 수 있어 서비스가 고객에게 주는 흥미도를 떨어뜨릴 것으로 판단
- 누군가가 너무 심하게 적거나, 너무 많지 않도록 어느정도 중간 구간에 모여있도록 처리
- 그래도 사람수가 너무 적으면 극단적인 결과에 가깝게 나오고, 너무 많으면 1/N 값에 수렴하게 됨

여러개의 서버에서 구동될 경우 데이터 정합성 보장과 성능
- (부족한 개발시간으로) 세밀하게 고민할 수가 없어 트랜잭션 중첩이 아예 없도록 단순하게 일차원적으로 구현하였음
- 줍기의 경우는 일련의 과정 중 데이터가 변동되어서는 안되므로 격리수준을 높임.(lock)
- 반대로 조회의 경우는 트랜잭션을 아예 적용하지 않아도 무방하다고 판단하였음
- 부득이하게 mySQL 을 사용하였으나 MongoDB나 캐시를 함께 사용하면 대규모 트래픽도 감당할 것으로 예상 
- (시간이 충분하다면) 분산 트랜잭션이나, 메시지큐를 이용하는 것이 대규모 서비스 구성에 바람직

### 테스트
필수 제약사항 시나리오별로 테스트코드 작성 (ThrowEventServiceTest)
- 돈뿌리기- 성공 및 전후 주최자의 잔고 비교
- 돈줍기- 비정상토큰이면 실패
- 돈줍기- 다른방사이람이면 실패
- 돈줍기- 자기자신은 실패
- 돈줍기- 성공 및 주운 사람의 전후 잔고 비교
- 돈줍기- 받고 또 시도하면 실패
- 돈줍기- 제한시간(10분) 초과면 실패
- 조회하기- 뿌린사람이 아니면 불가
- 조회하기- 7일 지나면 조회불가

테스트 시 admin API 없이 데이터 조작은 직접 MySQL 도구로 진행
