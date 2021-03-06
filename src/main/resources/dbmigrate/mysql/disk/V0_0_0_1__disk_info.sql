

-------------------------------------------------------------------------------
--  disk info
-------------------------------------------------------------------------------
CREATE TABLE DISK_INFO(
    ID BIGINT NOT NULL,
	NAME VARCHAR(200),
	DESCRIPTION VARCHAR(255),
	TYPE VARCHAR(50),
	FILE_SIZE BIGINT,
	CREATOR VARCHAR(64),
	CREATE_TIME DATETIME,
	REF VARCHAR(200),
	PREVIEW_STATUS VARCHAR(50),
	PREVIEW_REF VARCHAR(200),
	PARENT_ID BIGINT,
	PARENT_PATH VARCHAR(200),
	DIR_TYPE INT,
	PRIORITY INT,

	LAST_MODIFIER VARCHAR(64),
	LAST_MODIFIED_TIME DATETIME,
	STATUS VARCHAR(50),
	EXPIRE_TIME DATETIME,
	CHECKOUT_STATUS VARCHAR(50),
	FILE_VERSION VARCHAR(50),
	SECURITY_LEVEL VARCHAR(50),
    CONSTRAINT PK_DISK_INFO PRIMARY KEY(ID),
	CONSTRAINT FK_DISK_INFO_PARENT FOREIGN KEY (PARENT_ID) REFERENCES DISK_INFO(ID)
) ENGINE=INNODB CHARSET=UTF8;

CREATE INDEX IDX_DISK_INFO_NAME ON DISK_INFO(NAME);

























