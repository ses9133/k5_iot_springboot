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