CREATE TABLE dragon (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(20),
    countOfHeads INTEGER,
    priceForDay INTEGER
 );

 CREATE TABLE customer (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    firstName VARCHAR(20),
    secondName VARCHAR(20),
    phone VARCHAR(13)
 );

 CREATE TABLE lease (
    id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    dragon ID BIGINT,
    customer ID BIGINT,
    startDate DATE,
    endDate DATE,
    price INTEGER
 );
