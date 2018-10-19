CREATE TABLE employees(
 employee_id VARCHAR (60) NOT NULL,
 type VARCHAR (100) NOT NULL,
 first_name VARCHAR (100),
 middle_name VARCHAR (100),
 last_name VARCHAR (100) NOT NULL,
 passport_number VARCHAR (100),
 position VARCHAR (100),
 timestamp timestamp NOT NULL,
 active BOOLEAN NOT NULL,
 version INTEGER NOT NULL,
 PRIMARY KEY(employee_id, version)
);

CREATE TABLE employee_addresses(
 employee_id VARCHAR (60) NOT NULL,
 address_line_1 VARCHAR (255) NOT NULL,
 address_line_2 VARCHAR (255),
 city VARCHAR (100) NOT NULL,
 state VARCHAR (100) NOT NULL,
 zip_code VARCHAR (10) NOT NULL,
 version INTEGER NOT NULL,
 FOREIGN KEY(employee_id, version) references employees(employee_id, version)
);