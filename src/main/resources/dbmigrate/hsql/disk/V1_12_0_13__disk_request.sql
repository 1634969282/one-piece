

-------------------------------------------------------------------------------
--  disk request
-------------------------------------------------------------------------------
CREATE TABLE DISK_REQUEST(
    ID BIGINT NOT NULL,
    MASK INT,
    DESCRIPTION VARCHAR(200),
    RESULT VARCHAR(200),

    INFO_ID BIGINT,
    USER_ID VARCHAR(64),

	CREATOR VARCHAR(64),
	CREATE_TIME DATETIME,
	UPDATER VARCHAR(64),
	UPDATE_TIME DATETIME,
	STATUS VARCHAR(50),
    TENANT_ID VARCHAR(64),
    CONSTRAINT PK_DISK_REQUEST PRIMARY KEY(ID),
    CONSTRAINT FK_DISK_REQUEST_INFO FOREIGN KEY(INFO_ID) REFERENCES DISK_INFO(ID)
);

COMMENT ON TABLE DISK_REQUEST IS '下载';
COMMENT ON COLUMN DISK_REQUEST.ID IS 'id';
COMMENT ON COLUMN DISK_REQUEST.MASK IS '权限';
COMMENT ON COLUMN DISK_REQUEST.DESCRIPTION IS '备注';
COMMENT ON COLUMN DISK_REQUEST.RESULT IS '审批结果';
COMMENT ON COLUMN DISK_REQUEST.INFO_ID IS '节点id';
COMMENT ON COLUMN DISK_REQUEST.USER_ID IS '用户';
COMMENT ON COLUMN DISK_REQUEST.CREATOR IS '创建人';
COMMENT ON COLUMN DISK_REQUEST.CREATE_TIME IS '创建时间';
COMMENT ON COLUMN DISK_REQUEST.UPDATER IS '更新人';
COMMENT ON COLUMN DISK_REQUEST.UPDATE_TIME IS '更新时间';
COMMENT ON COLUMN DISK_REQUEST.TENANT_ID IS '租户';

