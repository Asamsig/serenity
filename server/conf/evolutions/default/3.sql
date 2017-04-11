# USER TABLE

# --- !Ups

CREATE TABLE IF NOT EXISTS users (
  user_id    VARCHAR(40) NOT NULL,
  first_name VARCHAR(255),
  last_name  VARCHAR(255),
  phone      VARCHAR(40),
  address    VARCHAR(255),
  created    TIMESTAMP    NOT NULL,
  updated    TIMESTAMP    NOT NULL,
  PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS user_emails (
  user_id         VARCHAR(40) NOT NULL,
  email           VARCHAR(255) NOT NULL,
  validated       BOOLEAN      NOT NULL,
  primary_address BOOLEAN      NOT NULL,
  PRIMARY KEY (email)
);

CREATE TABLE IF NOT EXISTS user_credentials (
  user_id  VARCHAR(40) NOT NULL,
  type     INTEGER      NOT NULL,
  password VARCHAR(255) NOT NULL,
  salt     VARCHAR(255),
  PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS user_memberships (
  user_id        VARCHAR(40) NOT NULL,
  valid_from     TIMESTAMP    NOT NULL,
  valid_to       TIMESTAMP    NOT NULL,
  issuer         INTEGER      NOT NULL,
  eb_attendee_id VARCHAR(40),
  eb_event_id    VARCHAR(40),
  eb_order_id    VARCHAR(40)
);

CREATE TABLE IF NOT EXISTS user_roles (
  user_id VARCHAR(40) NOT NULL,
  role    varchar(20)      NOT NULL
);

# --- !Downs

DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS user_emails;
DROP TABLE IF EXISTS user_credentials;
DROP TABLE IF EXISTS user_memberships;
DROP TABLE IF EXISTS user_roles;
