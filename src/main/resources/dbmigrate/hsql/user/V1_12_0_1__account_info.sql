

alter table ACCOUNT_INFO add column SOURCE VARCHAR(100);
alter table ACCOUNT_INFO add column REF VARCHAR(100);
alter table ACCOUNT_INFO add column DIRECTOR VARCHAR(100);

COMMENT ON COLUMN ACCOUNT_INFO.SOURCE IS '来源';
COMMENT ON COLUMN ACCOUNT_INFO.REF IS '外部标识';
COMMENT ON COLUMN ACCOUNT_INFO.DIRECTOR IS '负责人';

