

ALTER TABLE TICKET_INFO ADD COLUMN PRIORITY INT;
ALTER TABLE TICKET_INFO ADD COLUMN SOURCE VARCHAR(50);
ALTER TABLE TICKET_INFO ADD COLUMN RESOLVE_TIME TIMESTAMP;
ALTER TABLE TICKET_INFO ADD COLUMN RESOLVE_STATUS VARCHAR(50);
ALTER TABLE TICKET_INFO ADD COLUMN CLOSE_TIME TIMESTAMP;
ALTER TABLE TICKET_INFO ADD COLUMN CLOSE_STATUS VARCHAR(50);

COMMENT ON COLUMN TICKET_INFO.PRIORITY IS '优先级';
COMMENT ON COLUMN TICKET_INFO.SOURCE IS '来源';
COMMENT ON COLUMN TICKET_INFO.RESOLVE_TIME IS '解决时间';
COMMENT ON COLUMN TICKET_INFO.RESOLVE_STATUS IS '解决状态';
COMMENT ON COLUMN TICKET_INFO.CLOSE_TIME IS '关闭时间';
COMMENT ON COLUMN TICKET_INFO.CLOSE_STATUS IS '关闭状态';
