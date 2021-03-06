

-------------------------------------------------------------------------------
--  disk upload
-------------------------------------------------------------------------------
CREATE TABLE DISK_UPLOAD(
    ID BIGINT NOT NULL,
    NAME VARCHAR(200),
    TYPE VARCHAR(50),
    SIZE BIGINT,
    LAST_MODIFIED BIGINT,
    CATALOG VARCHAR(10),

    USER_ID VARCHAR(64),
    INFO_ID BIGINT,
    FILE_ID BIGINT,
    FOLDER_ID BIGINT,
    FOLDER_PATH VARCHAR(200),
    PART_TYPE VARCHAR(50),
    PART_INDEX INT,
    PART_PARENT BIGINT,
    RANGE_START BIGINT,
    RANGE_END BIGINT,

    START_TIME TIMESTAMP,
    END_TIME TIMESTAMP,
    REASON VARCHAR(200),

	CREATOR VARCHAR(64),
	CREATE_TIME DATETIME,
	UPDATER VARCHAR(64),
	UPDATE_TIME DATETIME,
	STATUS VARCHAR(50),
    TENANT_ID VARCHAR(64),
    CONSTRAINT PK_DISK_UPLOAD PRIMARY KEY(ID)
) ENGINE=INNODB CHARSET=UTF8;




























