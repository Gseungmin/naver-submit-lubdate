## QueueService.java
메시지 큐를 활용한 서비스 코드
커플간 메시지 동기화 시 활용
### 개선 전
준비과정에서 레디스 장애시 문제 대처가 없다는 것을 발견
### 개선 후
레디스 장애 발생 시, 로컬 캐시를 활용통해 관리

## StoryCacheService.java
벌크 업데이트를 위한 캐시 서비스 코드
벌크 업데이트 전 미리 데이터를 캐시 저장하는 로직
### 개선 전
준비과정에서 레디스 장애시 문제 대처가 없다는 것을 발견
### 개선 후
레디스 장애 발생 시, 바로 데이터베이스에 반영하도록 수정
