

ALTER TABLE DISK_SHARE ADD COLUMN CATALOG VARCHAR(50);

COMMENT ON COLUMN DISK_SHARE.CATALOG IS '类型';

