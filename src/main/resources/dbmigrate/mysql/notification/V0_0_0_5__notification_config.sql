

-------------------------------------------------------------------------------
--  notification config
-------------------------------------------------------------------------------
CREATE TABLE NOTIFICATION_CONFIG(
    ID BIGINT AUTO_INCREMENT,
    CODE VARCHAR(50),
    NAME VARCHAR(50),
    CONTENT TEXT,

    PRIORITY INT,
    CREATE_TIME TIMESTAMP,
    CREATOR VARCHAR(64),
    UPDATE_TIME TIMESTAMP,
    UPDATER VARCHAR(64),
    STATUS VARCHAR(50),

    APP VARCHAR(50),

    CATALOG_ID BIGINT,

    CONSTRAINT PK_NOTIFICATION_CONFIG PRIMARY KEY(ID),
    CONSTRAINT FK_NOTIFICATION_CONFIG_CATALOG FOREIGN KEY (CATALOG_ID) REFERENCES NOTIFICATION_CATALOG(ID)
) ENGINE=INNODB CHARSET=UTF8;















