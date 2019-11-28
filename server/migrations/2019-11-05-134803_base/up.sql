CREATE TABLE children (
    id SERIAL PRIMARY KEY,
    username VARCHAR NOT NULL
);

CREATE TABLE guardians (
    id SERIAL PRIMARY KEY,
    username VARCHAR NOT NULL,
    password VARCHAR NOT NULL,
    UNIQUE (username)
);

CREATE TABLE guardian_has_children (
    child_id INTEGER NOT NULL,
    guardian_id INTEGER NOT NULL,
    FOREIGN KEY (child_id) REFERENCES children,
    FOREIGN KEY (guardian_id) REFERENCES guardians,
    PRIMARY KEY (child_id, guardian_id)
);

CREATE TABLE locations (
    id SERIAL PRIMARY KEY,
    child_id INTEGER NOT NULL,
    location VARCHAR NOT NULL,
    FOREIGN KEY (child_id) REFERENCES children
);
