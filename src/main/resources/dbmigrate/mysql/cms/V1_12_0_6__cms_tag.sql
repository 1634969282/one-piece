

ALTER TABLE CMS_TAG ADD COLUMN CODE VARCHAR(50);
ALTER TABLE CMS_TAG ADD COLUMN SITE_ID BIGINT;
ALTER TABLE CMS_TAG ADD FOREIGN KEY (SITE_ID) REFERENCES CMS_SITE(ID);

