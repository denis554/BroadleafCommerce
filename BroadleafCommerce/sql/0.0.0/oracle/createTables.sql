DROP TABLE blc_customer_address;
DROP TABLE blc_customer;
DROP TABLE blc_challenge_question;

CREATE TABLE blc_challenge_question
(
  QUESTION_ID NUMBER(19,0) NOT NULL,
  QUESTION VARCHAR2(255)
  CONSTRAINT PK_BLC_CHALLENGE_QUESTION PRIMARY KEY(QUESTION_ID) USING INDEX TABLESPACE WEB_IDX1
);

CREATE TABLE blc_customer
(
  CUSTOMER_ID NUMBER(19,0) NOT NULL,
  CHALLENGE_ANSWER VARCHAR2(255) ,
  CHALLENGE_QUESTION_ID NUMBER(19,0) ,
  EMAIL_ADDRESS VARCHAR2(255) ,
  FIRST_NAME VARCHAR2(255) ,
  LAST_NAME VARCHAR2(255) ,
  PASSWORD VARCHAR2(255) ,
  PASSWORD_CHANGE_REQUIRED NUMBER(1,0) ,
  USER_NAME VARCHAR2(255) ,
  RECEIVE_EMAIL NUMBER(1,0) ,
  CONSTRAINT PK_BLC_CUSTOMER PRIMARY KEY(CUSTOMER_ID) USING INDEX TABLESPACE WEB_IDX1,
  CONSTRAINT cust_ques_fk FOREIGN KEY (CHALLENGE_QUESTION_ID) REFERENCES blc_challenge_question(QUESTION_ID)
);

CREATE UNIQUE INDEX IX1_BLC_CUSTOMER ON blc_customer (USER_NAME) USING INDEX TABLESPACE WEB_IDX1;

CREATE TABLE blc_customer_address
(
  ADDRESS_ID NUMBER(19,0) NOT NULL,
  ADDRESS_LINE1 VARCHAR2(255) ,
  ADDRESS_LINE2 VARCHAR2(255) ,
  ADDRESS_NAME VARCHAR2(255) ,
  CITY VARCHAR2(255) ,
  COUNTRY NUMBER(11,0) ,
  POSTAL_CODE VARCHAR2(255) ,
  STANDARDIZED NUMBER(1,0) ,
  STATE_PROV_REGION VARCHAR2(255) ,
  TOKENIZED_ADDRESS VARCHAR2(255) ,
  ZIP_FOUR VARCHAR2(255) ,
  CUSTOMER_ID NUMBER(19,0) NOT NULL,
  COMPANY_NAME VARCHAR2(255) ,
  IS_DEFAULT NUMBER(1,0) ,
  IS_ACTIVE NUMBER(1,0) ,
  FIRST_NAME VARCHAR2(255) ,
  LAST_NAME VARCHAR2(255) ,
  PRIMARY_PHONE VARCHAR2(255) ,
  SECONDARY_PHONE VARCHAR2(255) ,
  CONSTRAINT PK_BLC_CUSTOMER_ADDRESS PRIMARY KEY(ADDRESS_ID) USING INDEX TABLESPACE WEB_IDX1,
  CONSTRAINT address_customer_fk FOREIGN KEY (CUSTOMER_ID) REFERENCES blc_customer(CUSTOMER_ID)
);

------------------------
-- INSERT TEST CHALLENGE QUESTIONS
------------------------
INSERT INTO blc_challenge_question ( QUESTION_ID, QUESTION ) VALUES ( 1, 'What is your place of birth?' );;
INSERT INTO blc_challenge_question ( QUESTION_ID, QUESTION ) VALUES ( 2, 'What is your Mother''s maiden name?' );
INSERT INTO blc_challenge_question ( QUESTION_ID, QUESTION ) VALUES ( 3, 'What is the name of your favorite pet?' );

------------------------
-- INSERT TEST CUSTOMER
------------------------
INSERT INTO blc_customer
  ( CUSTOMER_ID, USER_NAME, PASSWORD, PASSWORD_CHANGE_REQUIRED, RECEIVE_EMAIL )
  VALUES ( 1, 'rod', '16d7a4fca7442dda3ad93c9a726597e4', 1, 1 );

insert into blc_customer_address (ADDRESS_ID, ADDRESS_LINE1, ADDRESS_NAME, CITY, POSTAL_CODE, STATE_PROV_REGION, CUSTOMER_ID, IS_DEFAULT, IS_ACTIVE) values (1, '1 a way', 'home', 'Frisco', '75035', 'TX', 37, 1, 1);
