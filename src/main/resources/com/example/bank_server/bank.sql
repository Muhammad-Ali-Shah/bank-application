CREATE DATABASE bank
    WITH
    OWNER = postgres
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;

\c bank
CREATE TABLE IF NOT EXISTS records
(
    sort_code      text NOT NULL,
    account_number text NOT NULL,
    first_name     text,
    surname        text,
    dob            text,
    balance        text,
    PRIMARY KEY (sort_code, account_number)
);


