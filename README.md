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

