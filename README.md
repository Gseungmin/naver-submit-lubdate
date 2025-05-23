## 보안 관련 문제로 네이버 제출용으로 필수 코드만 담은 임시 리포지토리 생성

## 서비스 구현

### 1️⃣ 아키텍처 문서화

[문서화](https://jseungmin.notion.site/1b0e2fd91ae280ed9a19e80d486f1d13?pvs=4)


### 2️⃣ 메시지 큐 아키텍처 도입

[문서화](https://jseungmin.notion.site/ACK-1b0e2fd91ae28094a8a1dd045319a2fd?pvs=4) [PR](https://github.com/Gseungmin/naver-submit-lubdate/pull/1)

1. 커플서비스는 둘만 데이터 동기화진행하면 되는 구조이다
2. 이런 구조에서 매번 데이터베이스를 통해 조회가 발생하는건 비효율적이다
3. 메시지 큐를 사용하고, 이를 ZSET으로 구현하므로 성능을 개선한다
4. 또한 로컬 캐시를 통한 장애를 조치한다

### 3️⃣ 벌크 업데이트를 통한 성능 개선

[PR](https://github.com/Gseungmin/naver-submit-lubdate/pull/2)

[벌크 업데이트](https://jseungmin.notion.site/1aee2fd91ae280248179e4b60c9a3220?pvs=4)

1. 커플이 메시지 큐를 통해 데이터를 동기화하지만 서버 데이터베이스에도 반영이 필요하다
2. 하지만 이미 커플은 메시지 큐를 통해 동기화를 진행하므로 바로 서버에 반영할 필요가 없다
3. 따라서 캐시를 통해 벌크 업데이트를 진행한다
4. 또한 클라이언트가 재로그인 시 서버에서 데이터를 가지고 오는데 이때 데이터베이스는 최신 상태가 아닐 수 있다(캐시로인해)
5. 따라서 캐시에 업데이트 사항을 체크해야하는데 이때 개별 체크는 네트워크 레이턴시를 발생시킨다
6. 이를 파이프라인으로 성능을 개선한다

### 4️⃣ 파이프라인 도입과 탈 파이프라인

[PR](https://github.com/Gseungmin/naver-submit-lubdate/pull/4)

[파이프라인을 쓴 이유](https://jseungmin.notion.site/REDIS-1afe2fd91ae280628e12f8d49a112307?pvs=4)
[파이프라인을 쓰지 않은 이유](https://jseungmin.notion.site/1fce2fd91ae280138034efb708878486?pvs=4)

1. RTT를 줄이기 위해 파이프라인을 썼다.
2. 하지만 파이프라인은 요청을 묶어서 보내지만, 각 요청을 개별 처리한다.
3. 즉, 여기서 발생하는 오버헤드가 존재하는 것이다.
4. 이를 개선하고자 파이프라인을 사용하는 것이 아닌 레디스에서 제공하는 API를 사용했다
