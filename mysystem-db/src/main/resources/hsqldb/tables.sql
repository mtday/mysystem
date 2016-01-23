
DROP TABLE companies IF EXISTS;


CREATE TABLE companies (
    id             INTEGER      GENERATED BY DEFAULT AS IDENTITY
                                  (START WITH 1, INCREMENT BY 1) NOT NULL PRIMARY KEY,
    name           VARCHAR(250) NOT NULL,
    active         BOOLEAN      DEFAULT TRUE NOT NULL,

    CONSTRAINT unique_company_name UNIQUE (name)
);
