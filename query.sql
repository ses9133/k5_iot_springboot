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

-- 0812 (B_Student)
CREATE TABLE IF NOT EXISTS students (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
	UNIQUE KEY uq_name_email (name, email) -- unique key 설정 방법
);

DROP TABLE students;

SELECT * FROM students;

-- 0813 (C_Book)

CREATE TABLE IF NOT EXISTS books(
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
    writer VARCHAR(50) NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(500) NOT NULL,
    category VARCHAR(20) NOT NULL,
    -- 자바 enum 데이터 처리
    -- : DB 에서는 VARCHAR(문자열) 로 관리 + CHECK 제약 조건으로 문자 제한
    CONSTRAINT chk_book_category CHECK (category IN ('NOVEL', 'ESSAY', 'POEM', 'MAGAZINE')),
    -- 동일 (저자 + 제목) 세트 중복 저장 방지
    CONSTRAINT uk_book_writer_title UNIQUE (writer, title)
);

SELECT * FROM books;

-- 08/19(D_Post, D_Comment)
CREATE TABLE IF NOT EXISTS posts (
	id BIGINT AUTO_INCREMENT,
	title VARCHAR(200) NOT NULL COMMENT '게시글 제목',
    content LONGTEXT NOT NULL COMMENT '게시글 내용', -- @Lob 매핑 대응
    author VARCHAR(100) NOT NULL COMMENT '작성자 표시명 또는 ID'    ,
    PRIMARY KEY(`id`),
    KEY `idx_post_author` (`author`)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4,
  COLLATE = utf8mb4_unicode_ci,
  COMMENT = '게시글';

DESC posts;
DROP TABLE posts;

-- 댓글 테이블
CREATE TABLE IF NOT EXISTS comments (
	id BIGINT AUTO_INCREMENT,
    post_id BIGINT NOT NULL COMMENT 'posts.id FK',
    content VARCHAR(1000) NOT NULL COMMENT '댓글 내용',
    commenter VARCHAR(100) NOT NULL COMMENT '댓글 작성자 표시명 또는 ID',
    PRIMARY KEY(id),
    KEY idx_comment_post_id (post_id),
    -- 인덱스 생성 명령어 (INDEX, KEY 동일함), idx_comment_post_id: 생성할 인덱스 이름 , (post_id): () 내부에는 인덱스 적용할 컬럼이름 작성
    KEY idx_comment_commenter(commenter),
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES posts (id) 
    -- fk_comment_post: 제약 조건 이름
    ON DELETE CASCADE 
    ON UPDATE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4,
  COLLATE = utf8mb4_unicode_ci,
  COMMENT = '댓글';
  
  SELECT * FROM posts;
  SELECT * FROM comments;
  
  DESC comments;
