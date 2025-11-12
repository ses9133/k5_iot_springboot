CREATE TABLE refresh_tokens (
	id bigint primary key auto_increment,
    username varchar(100) not null,
    token varchar(512) not null,
	expiry bigint not null,
    
    UNIQUE KEY `uk_refresh_username` (username),
    UNIQUE KEY `uk_refresh_token` (token)
    
)	ENGINE = InnoDB
	DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci
    COMMENT =  'JWT Refresh Token 저장 테이블';

SELECT * FROM refresh_tokens;
    
CREATE TABLE trucks (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    region VARCHAR(50),
    description VARCHAR(255)  ,
    CONSTRAINT fk_trucks_user FOREIGN KEY (owner_id) REFERENCES users(id)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '푸드트럭 테이블';

CREATE TABLE reservations (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
    truck_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    time_slot VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_reservations_truck FOREIGN KEY (truck_id) REFERENCES trucks(id),
    CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users(id)	
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '푸드트럭 테이블';

select * from users;

INSERT INTO trucks (owner_id, name, category, region, description)
VALUES
	(1, '한강 푸드트럭', 'DESSERT', 'SEOUL', '디저트를 파는 한강의 푸드트럭'),
    (2, '광안리 푸드트럭', 'FOODS', 'BUSAN', '음식을 파는 광안리의 푸드트럭'),
    (3, '유성 푸드트럭', 'DRINKS', 'DAEJEON', '음료를 파는 유성의 푸드트럭');

SELECT * FROM trucks;

SELECT * FROM users;

INSERT INTO reservations (truck_id, user_id, date, time_slot, status)
VALUES
	(7, 1, '2025-11-10', '10:00-11:00', 'CONFIRMED'),
	(7, 2, '2025-11-10', '11:00-12:00', 'PENDING'),
	(7, 3, '2025-11-11', '12:00-13:00', 'CONFIRMED'),
	(8, 1, '2025-11-11', '10:00-11:00', 'CANCELLED'),
	(8, 2, '2025-11-11', '10:00-11:00', 'PENDING'),
	(8, 3, '2025-11-12', '12:00-13:00', 'CONFIRMED'),
	(8, 2, '2025-11-12', '12:00-13:00', 'CONFIRMED'),
	(9, 2, '2025-11-12', '11:00-12:00', 'PENDING'),
	(9, 3, '2025-11-13', '11:00-12:00', 'CANCELLED'),
	(9, 1, '2025-11-13', '12:00-13:00', 'PENDING');

SELECT * FROM reservations;