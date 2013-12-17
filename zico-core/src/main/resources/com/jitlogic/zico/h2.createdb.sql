CREATE TABLE IF NOT EXISTS SYMBOLS (
  SID  INTEGER PRIMARY KEY NOT NULL,
  NAME VARCHAR(1024)       NOT NULL
);

INSERT INTO SYMBOLS (SID, NAME) VALUES (0, '<INVALID>');


CREATE TABLE IF NOT EXISTS HOSTS (
  HOST_ID    INTEGER AUTO_INCREMENT NOT NULL,
  HOST_NAME  VARCHAR(128)           NOT NULL,
  HOST_ADDR  VARCHAR(128),
  HOST_PATH  VARCHAR(128)           NOT NULL,
  HOST_PASS  VARCHAR(128),
  HOST_FLAGS INTEGER                NOT NULL DEFAULT 0,
  MAX_SIZE   BIGINT                 NOT NULL DEFAULT 1073741824,
  HOST_DESC  VARCHAR(255),
  PRIMARY KEY (HOST_ID)
);


CREATE TABLE IF NOT EXISTS TRACES (
  HOST_ID   INTEGER NOT NULL,
  DATA_OFFS BIGINT  NOT NULL,
  TRACE_ID  INTEGER NOT NULL,
  DATA_LEN  INTEGER NOT NULL,
  INDEX_OFFS BIGINT NOT NULL,
  INDEX_LEN INTEGER NOT NULL,
  CLOCK     BIGINT  NOT NULL,
  RFLAGS    INTEGER NOT NULL,
  TFLAGS    INTEGER NOT NULL,
  STATUS    INTEGER NOT NULL,
  CLASS_ID  INTEGER NOT NULL,
  METHOD_ID INTEGER NOT NULL,
  SIGN_ID   INTEGER NOT NULL,
  CALLS     BIGINT  NOT NULL,
  ERRORS    BIGINT  NOT NULL,
  RECORDS   BIGINT  NOT NULL,
  EXTIME    BIGINT  NOT NULL,
  ATTRS     LONGTEXT,
  EXINFO    LONGTEXT,
  PRIMARY KEY (HOST_ID, DATA_OFFS)
);


CREATE TABLE IF NOT EXISTS TEMPLATES (
  TEMPLATE_ID   INTEGER AUTO_INCREMENT NOT NULL,
  TRACE_ID      INTEGER                NOT NULL,
  ORDER_NUM     INTEGER                NOT NULL,
  FLAGS         INTEGER                NOT NULL DEFAULT 0,
  COND_TEMPLATE VARCHAR(255),
  COND_PATTERN  VARCHAR(255),
  TEMPLATE      VARCHAR(255)           NOT NULL,
  PRIMARY KEY (TEMPLATE_ID)
);

CREATE TABLE IF NOT EXISTS TRACE_TYPES (
  HOST_ID  INTEGER NOT NULL,
  TRACE_ID INTEGER NOT NULL,
  PRIMARY KEY (HOST_ID, TRACE_ID)
);

CREATE TABLE IF NOT EXISTS USERS (
  USER_ID   INTEGER AUTO_INCREMENT NOT NULL,
  USER_NAME VARCHAR(64) UNIQUE     NOT NULL,
  REAL_NAME VARCHAR(255)           NOT NULL,
  FLAGS     INTEGER                NOT NULL DEFAULT 0,
  PRIMARY KEY (USER_ID)
);

CREATE TABLE IF NOT EXISTS USERS_HOSTS (
  USER_ID INTEGER NOT NULL,
  HOST_ID INTEGER NOT NULL,
  FLAGS   INTEGER NOT NULL DEFAULT 0,
  PRIMARY KEY (USER_ID, HOST_ID)
);

COMMIT;
