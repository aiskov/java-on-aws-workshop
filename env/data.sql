CREATE TABLE service_db.product (
    id varchar(36) not null primary key,
    name varchar(255) not null null
) engine=InnoDB;

INSERT INTO service_db.product (id, name)
VALUES ('89271e89-b332-46c4-bdff-523831c55591', 'Cup'),
       ('53e59ad3-8cc6-486a-bb12-fd148e6e1ef0', 'Another Cup');

CREATE TABLE service_db.product_file (
    name varchar(255) not null primary key,
    product_id varchar(36) not null
) engine=InnoDB;

create index product_file_product_id_fk
    on service_db.product_file (product_id);

INSERT INTO service_db.product_file (product_id, name)
VALUES ('89271e89-b332-46c4-bdff-523831c55591', 'lucas-oliveira-DzJOCovkkME-unsplash.jpg'),
       ('53e59ad3-8cc6-486a-bb12-fd148e6e1ef0', 'stephen-andrews-Ky2tMJZdMLc-unsplash.jpg'),
       ('53e59ad3-8cc6-486a-bb12-fd148e6e1ef0', 'stephen-andrews-R49DKw0erLY-unsplash.jpg');

CREATE TABLE service_db.user (
    username varchar(32) not null primary key,
    password varchar(32) not null
) engine=InnoDB;

INSERT INTO service_db.user (username, password)
VALUES ('root', 'toor');