

-------------------------------------------------------------------------------
--  account device
-------------------------------------------------------------------------------
CREATE TABLE ACCOUNT_DEVICE(
    ID BIGINT NOT NULL,
    CODE VARCHAR(64),
    TYPE VARCHAR(64),
    OS VARCHAR(100),
    CLIENT VARCHAR(100),
    STATUS VARCHAR(50),
    CREATE_TIME DATETIME,
    LAST_LOGIN_TIME DATETIME,
    ATTRIBUTE1 VARCHAR(200),
    ACCOUNT_ID BIGINT,
    TENANT_ID VARCHAR(64),
    CONSTRAINT PK_ACCOUNT_DEVICE PRIMARY KEY(ID),
    CONSTRAINT FK_ACCOUNT_DEVICE_ACCOUNT FOREIGN KEY(ACCOUNT_ID) REFERENCES ACCOUNT_INFO(ID)
) ENGINE=INNODB CHARSET=UTF8;

