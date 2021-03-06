

-------------------------------------------------------------------------------
--  ticket info
-------------------------------------------------------------------------------
CREATE TABLE TICKET_INFO(
    ID BIGINT NOT NULL,
    CODE VARCHAR(200),
    NAME VARCHAR(200),
    CONTENT VARCHAR(65535),
    CREATOR VARCHAR(64),
    STATUS VARCHAR(50),
    CREATE_TIME DATETIME,
    ASSIGNEE VARCHAR(64),
    UPDATE_TIME DATETIME,
    CATALOG_ID BIGINT,
    GROUP_ID BIGINT,
    CONSTRAINT PK_TICKET_INFO PRIMARY KEY(ID),
    CONSTRAINT FK_TIKCET_INFO_CATALOG FOREIGN KEY (CATALOG_ID) REFERENCES TICKET_CATALOG(ID),
    CONSTRAINT FK_TICKET_INFO_GROUP FOREIGN KEY (GROUP_ID) REFERENCES TICKET_GROUP(ID)
);

COMMENT ON TABLE TICKET_INFO IS '工单信息';
COMMENT ON COLUMN TICKET_INFO.ID IS 'ID';
COMMENT ON COLUMN TICKET_INFO.CODE IS '编号';
COMMENT ON COLUMN TICKET_INFO.NAME IS '名称';
COMMENT ON COLUMN TICKET_INFO.CONTENT IS '内容';
COMMENT ON COLUMN TICKET_INFO.CREATOR IS '创建人';
COMMENT ON COLUMN TICKET_INFO.STATUS IS '状态';
COMMENT ON COLUMN TICKET_INFO.CREATE_TIME IS '创建时间';
COMMENT ON COLUMN TICKET_INFO.ASSIGNEE IS '分配人';
COMMENT ON COLUMN TICKET_INFO.UPDATE_TIME IS '更新时间';
COMMENT ON COLUMN TICKET_INFO.CATALOG_ID IS '分类';
COMMENT ON COLUMN TICKET_INFO.GROUP_ID IS '群组';

