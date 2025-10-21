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

-- 0821(F_Board)
-- 게시판 테이블 (생성/수정 시간 포함)
CREATE TABLE IF NOT EXISTS boards (
	id BIGINT AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL COMMENT '게시판 제목',
    content LONGTEXT NOT NULL COMMENT '게시판 내용',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY(id)
) ENGINE=InnoDB
DEFAULT CHARSET = utf8mb4,
COLLATE = utf8mb4_unicode_ci
COMMENT = '게시글';

SELECT * FROM boards;

-- 08/27 (G_User_role)
-- 사용자 권한 테이블
#### 사용하지 않음 #####
# : 아래의 사용자-권한 다대다 형식 사용 권장
SELECT * FROM users;
DROP TABLE IF EXISTS user_roles;
CREATE TABLE IF NOT EXISTS user_roles (
	user_id BIGINT NOT NULL,
    role VARCHAR(30) NOT NULL,
    
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_roles UNIQUE (user_id, role),
    CONSTRAINT chk_user_roles_role CHECK (role IN ('USER', 'MANAGER', 'ADMIN'))
) ENGINE=InnoDB
DEFAULT CHARSET = utf8mb4,
COLLATE = utf8mb4_unicode_ci
COMMENT = '사용자 권한';

-- users 테이블
CREATE TABLE IF NOT EXISTS `users` (
	id BIGINT NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT `uk_users_login_id` UNIQUE (login_id),
    CONSTRAINT `uk_users_email` UNIQUE (email),
    CONSTRAINT `uk_users_nickname` UNIQUE (nickname),
    CONSTRAINT `chk_users_gender` CHECK(gender IN ('MALE', 'FEMALE'))
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '사용자';
  
-- 0910 (G_Role)
-- 권한 코드 테이블
CREATE TABLE IF NOT EXISTS `roles` (
	role_name VARCHAR(30) PRIMARY KEY
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '권한 코드(USER, MANAGER, OWNER 등)';
  
-- 0910 G_UserRoleId (role 과 user (다대다 관계) 를 연결하는 중간 테이블)
-- 사용자-권한 매핑 (조인 엔티티)
CREATE TABLE IF NOT EXISTS `user_roles` (
	user_id BIGINT NOT NULL,
    role_name VARCHAR(30) NOT NULL,
    
    PRIMARY KEY (user_id, role_name),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES `users` (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_name) REFERENCES `roles` (role_name)
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '사용자 권한 매핑';
  
## 권한 데이터 삽입 
INSERT INTO roles (role_name) 
VALUES
	('USER'),
    ('MANAGER'),
    ('ADMIN')
    # 이미 값이 있는 경우 (DUPLICATE, 즉 중복된 경우), 에러 대신 그대로 유지할 것을 설정
    ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);
    
## 사용자 권한 매핑 삽입
INSERT INTO user_roles (user_id, role_name) 
VALUES 
	(1, 'USER'),
    (2, 'MANAGER'),
    (2, 'USER'),
    (3, 'ADMIN'),
    (3, 'MANAGER')
    ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);
    
SELECT * FROM roles;
    
SELECT * FROM user_roles;
SELECT * FROM users;
    
-- 09/01 (주문 관리 시스템)
-- 트랜잭션, 트리거, 인데긋, 뷰 학습
# products(상품), stocks(재고)
#, orders(주문 정보), order_items(주문 상세 정보), order_logs(주문 기록 정보)

-- 안전 실행: 삭제 순서
# FOREIGN_KEY_CHECKS: 외래 키 제약 조건을 활성화(1) 하거나 비활성화(0) 하는 명령어
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS order_logs;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS stocks;
DROP TABLE IF EXISTS products;
SET FOREIGN_KEY_CHECKS = 1;

-- 상품 정보 테이블
CREATE TABLE IF NOT EXISTS products (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL, 
    price INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    
    CONSTRAINT uq_products_name UNIQUE(name),
    INDEX idx_product_name (name) -- 제품명으로 인덱스 설정 => 제품 조회 시 성능 향상
) ENGINE=InnoDB						-- MySQL 에서 테이블이 데이터를 저장하고 관리하는 방식을 지정하는 명령어 
  DEFAULT CHARSET = utf8mb4			-- DB 나 테이블의 기본 문자 집합 (4바이트까지 지원 - 이모지 포함)
  COLLATE = utf8mb4_unicode_ci		-- 정렬 순서 지정 (대소문자 구분 없이 문자열 비교 정렬)
  COMMENT = '상품 정보';
  
  # ENGINE=InnoDB: 트랜잭션 지원(ACID), 외래키 제약 조건 지원(참조 무결성 보장) 
  
-- 재고 정보 테이블
  CREATE TABLE IF NOT EXISTS stocks (
	 id BIGINT AUTO_INCREMENT PRIMARY KEY,
     product_id BIGINT NOT NULL,
     quantity INT NOT NULL,
     created_at DATETIME(6) NOT NULL,
     updated_at DATETIME(6) NOT NULL,
     
	CONSTRAINT fk_stocks_product
		FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
	CONSTRAINT chk_stocks_qty CHECK (quantity >= 0), -- CHECK 제약 조건
    INDEX idx_stocks_product_id(product_id)
  ) ENGINE = InnoDB
	DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    COMMENT = '상품 재고 정보';
    
-- 주문 정보 테이블
  CREATE TABLE IF NOT EXISTS orders (
	id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    user_id BIGINT NOT NULL,
    order_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
	created_at DATETIME(6) NOT NULL,
	updated_at DATETIME(6) NOT NULL,
    
    CONSTRAINT fk_orders_user
		FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
	CONSTRAINT chk_orders_os CHECK (order_status IN ('PENDING', 'APPROVED', 'CANCELED')),
	INDEX idx_orders_user (user_id),
    INDEX idx_orders_status (order_status),
    INDEX idx_orders_created_at (created_at)
  ) ENGINE = InnoDB  
	DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    COMMENT = '주문 정보';
    
-- 주문 상세정보 테이블
  CREATE TABLE IF NOT EXISTS order_items (
	 id BIGINT AUTO_INCREMENT PRIMARY KEY,
     order_id BIGINT NOT NULL, 				-- 주문 정보
     product_id BIGINT NOT NULL,		    -- 제품 정보
     quantity INT NOT NULL,
	 created_at DATETIME(6) NOT NULL,
	 updated_at DATETIME(6) NOT NULL,
     
	CONSTRAINT fk_order_items_order
		FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
	CONSTRAINT fk_order_items_product
		FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
	CONSTRAINT chk_order_items_qty CHECK (quantity > 0),  -- 재고는 0 개가 가능하나 주문 수량은 0개가 될수 없음
	INDEX idx_order_items_order (order_id),
	INDEX idx_order_items_product (product_id),
    UNIQUE KEY uq_order_product (order_id, product_id)
  ) ENGINE = InnoDB
	DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    COMMENT = '주문 상세 정보';
    
-- 주문 내역 테이블
  CREATE TABLE IF NOT EXISTS order_logs (
	 id BIGINT AUTO_INCREMENT PRIMARY KEY,
     order_id BIGINT NOT NULL,
     message VARCHAR(255),
     created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
	 updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
     -- 트랜잭션이 아닌 트리거가 직접 INSERT 하는 로그 테이블은 시간 누락 방지를 위해 DB 기본값 유지
     
	CONSTRAINT fk_order_logs_order
		FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
	INDEX idx_order_logs_order (order_id),
    INDEX idx_order_logs_created_at (created_at)
  ) ENGINE = InnoDB
	DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    COMMENT =  '주문 기록 정보';
    
-- 초기 데이터 설정 --
INSERT INTO products (name, price, created_at, updated_at)
VALUES 
	('갤럭시 Z플립7', 50000, NOW(6), NOW(6)),
	('아이폰 16', 60000, NOW(6), NOW(6)),
	('갤릭서 S25 울트라', 55000, NOW(6), NOW(6)),
	('맥북 프로 14', 80000, NOW(6), NOW(6));
    
INSERT INTO stocks (product_id, quantity, created_at, updated_at)
VALUES 
	(1, 50, NOW(6), NOW(6)),
	(2, 30, NOW(6), NOW(6)),
	(3, 70, NOW(6), NOW(6)),
	(4, 20, NOW(6), NOW(6));
    
-- 0902
-- 뷰 (행 단위): 주문 상세 화면(API) - 한 주문의 각 상품 라인 아이템 정보를 상세하게 제공할 때 
-- : ex) GET /api/v1/orders/{orderId}/items
CREATE OR REPLACE VIEW order_summary AS
SELECT
	o.id					AS order_id,
    o.user_id				AS user_id,
    o.order_status 			AS order_status,
    p.name					AS product_name,
    oi.quantity				AS quantity,
    p.price					AS price,
    CAST((oi.quantity * p.price) AS SIGNED) AS total_price,
    o.created_at			AS ordered_at
FROM
	orders o
    JOIN order_items oi ON o.id = oi.order_id
    JOIN products p ON oi.product_id = p.id;
        
DROP VIEW order_summary;

-- 뷰 (주문 합계)
CREATE OR REPLACE VIEW order_totals AS
SELECT
	o.id						AS order_id,
    o.user_id					AS user_id,
    o.order_status				AS order_status,
    CAST(SUM(oi.quantity * p.price)	AS SIGNED) AS order_total_amount,
    CAST(SUM(oi.quantity) AS SIGNED)			AS order_total_qty,
    MIN(o.created_at)			AS ordered_at
FROM
    orders o
    JOIN order_items oi ON o.id = oi.order_id
    JOIN products p ON oi.product_id = p.id
GROUP BY 
	o.id, o.user_id, o.order_status; -- 주문 별! 합계: 주문(orders) 정보를 기준으로 그룹화!
    
DROP VIEW order_totals; -- REPLACE 명령어로 DROP 안써도 됨
    
-- 트리거: 주문 생성시 로그
# 고객 문의/장애 분석시 "언제 주문 레코드가 생겼는지" 원인 추적에 사용
DELIMITER // 
CREATE TRIGGER trg_after_order_insert
	AFTER INSERT ON orders
	FOR EACH ROW
    BEGIN
		INSERT INTO order_logs(order_id, message)
		VALUES (NEW.id, CONCAT('주문이 생성되었습니다. 주문 ID: ', NEW.id));
END //
DELIMITER ;

-- 트리거: 주문 상태 변경시 로그
# 상태 전이 추적시 "누가 언제 어떤 상태로 바꿨는지" 원인 추적에 사용
DELIMITER // 
CREATE TRIGGER trg_after_order_status_update
	AFTER UPDATE ON orders
	FOR EACH ROW
    BEGIN
		IF NEW.order_status <> OLD.order_status THEN -- A <> B는 A != B 를 의미
			INSERT INTO order_logs(order_id, message)
			VALUES (NEW.id, CONCAT('주문 상태가 ', OLD.order_status, ' -> ', NEW.order_status, '로 변경되었습니다.'));
		END IF;
END //
DELIMITER ;order_summary

-- 09/10

SELECT * FROM products;
SELECT * FROM orders;
SELECT * FROM stocks;
SELECT * FROM order_items;
SELECT * FROM order_logs;
SELECT * FROM users;

-- 10/21
create table notice (
	id bigint auto_increment primary key,
    title varchar(255) not null,
    content TEXT not null,
    author varchar(100) not null,
    created_at datetime default current_timestamp
);






















 
