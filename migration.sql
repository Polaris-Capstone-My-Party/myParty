DROP DATABASE IF EXISTS myparty_db;
CREATE DATABASE IF NOT EXISTS myparty_db;

USE myparty_db;


CREATE TABLE members(
                        id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                        username VARCHAR(240) NOT NULL,
                        email VARCHAR(240) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        phone INT(10),
                        first_name VARCHAR(240) NOT NULL,
                        last_name VARCHAR(240) NOT NULL,
                        PRIMARY KEY (id)
);

CREATE TABLE location(
                         id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                         address_one VARCHAR(240) NOT NULL,
                         address_two VARCHAR(240),
                         city VARCHAR(240) NOT NULL,
                         state VARCHAR(240) NOT NULL,
                         zipcode VARCHAR(240) NOT NULL,
                         PRIMARY KEY (id)
);

CREATE TABLE parties(
                        id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                        title VARCHAR(240) NOT NULL,
                        description VARCHAR(240) NOT NULL,
                        start_time DATETIME,
                        end_time DATETIME,
                        member_id INT UNSIGNED,
                        location_id INT UNSIGNED,
                        url_key BINARY(30),
                        PRIMARY KEY (id),
                        FOREIGN KEY (member_id) REFERENCES members(id) ,
                        FOREIGN KEY (location_id) REFERENCES location(id)
);

CREATE TABLE guests(
                       id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                       email VARCHAR(240) NOT NULL,
                       first_name VARCHAR(240) NOT NULL,
                       last_name VARCHAR(240) NOT NULL,
                       rsvp_status VARCHAR(240) NOT NULL,
                       party_id INT UNSIGNED,
                       PRIMARY KEY (id),
                       FOREIGN KEY (party_id) REFERENCES parties(id)
);

CREATE TABLE items(
                      id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                      name  VARCHAR(240) NOT NULL,
                      PRIMARY KEY (id)
);

CREATE TABLE party_items(
                            id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                            quantity INT,
                            party_id INT UNSIGNED,
                            item_id INT UNSIGNED,
                            guest_id INT UNSIGNED,
                            member_id INT UNSIGNED,
                            FOREIGN KEY (party_id) REFERENCES parties(id),
                            FOREIGN KEY (item_id) REFERENCES items(id),
                            FOREIGN KEY (guest_id) REFERENCES guests(id),
                            FOREIGN KEY (member_id) REFERENCES members(id),
                            PRIMARY KEY (id)
);

CREATE TABLE party_members(
                              id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                              party_id INT UNSIGNED,
                              member_id INT UNSIGNED,
                              rsvp_status VARCHAR(240) NOT NULL,
                              FOREIGN KEY (party_id) REFERENCES parties(id),
                              FOREIGN KEY (member_id) REFERENCES members(id),
                              PRIMARY KEY (id)

);

CREATE TABLE tags(
                     id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                     name  VARCHAR(240) NOT NULL,
                     PRIMARY KEY (id)
);

CREATE TABLE party_tags(
                          party_id INT UNSIGNED,
                          tag_id INT UNSIGNED,
                          FOREIGN KEY (party_id) REFERENCES parties(id),
                          FOREIGN KEY (tag_id) REFERENCES tags(id)
);

CREATE TABLE item_bringer(
                             id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                             quantity INT UNSIGNED,
                             guest_id INT UNSIGNED,
                             member_id INT UNSIGNED,
                             party_item INT UNSIGNED,
                             FOREIGN KEY (guest_id) REFERENCES guests(id),
                             FOREIGN KEY (member_id) REFERENCES members(id),
                             FOREIGN KEY (party_item) REFERENCES party_items(id),
                             PRIMARY KEY (id)
);
