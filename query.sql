DROP DATABASE IF EXISTS k5_iot_springboot;

CREATE DATABASE IF NOT EXISTS k5_iot_springboot
	CHARACTER SET utf8mb4 -- 문자셋
    COLLATE utf8mb4_general_ci; -- 정렬 설정
 
USE k5_iot_springboot; -- 스키마 선택은 자바 연결이랑 상관없음

-- 0811 (A_Test)
CREATE TABLE IF NOT EXISTS test (
	test_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL
);

SELECT * FROM test;
DESC test;

DROP TABLE test;
    
INSERT INTO test (name)
VALUES ('정은혜');

