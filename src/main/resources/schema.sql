DROP TABLE IF EXISTS users, items, bookings, requests, comments;

CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                     name VARCHAR(255) NOT NULL,
                                     email VARCHAR(255) NOT NULL,
                                     CONSTRAINT pk_user PRIMARY KEY (id),
                                     CONSTRAINT uq_user_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS items (
                                     id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                     name VARCHAR(255) NOT NULL,
                                     description VARCHAR(255) NOT NULL,
                                     is_available BOOLEAN,
                                     owner_id BIGINT,
                                     request_id BIGINT,
                                     CONSTRAINT pk_item PRIMARY KEY (id),
                                     CONSTRAINT fk_items_users FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
                                     );

CREATE TABLE IF NOT EXISTS bookings (
                                     id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                     start_date TIMESTAMP WITHOUT TIME ZONE,
                                     end_date TIMESTAMP WITHOUT TIME ZONE,
                                     item_id BIGINT,
                                     booker_id BIGINT,
                                     status VARCHAR(25),
                                     CONSTRAINT pk_booking PRIMARY KEY (id),
                                     CONSTRAINT fk_bookings_items FOREIGN KEY (item_id) REFERENCES items (id),
                                     CONSTRAINT fk_bookings_users FOREIGN KEY (booker_id) REFERENCES users (id)
);


CREATE TABLE IF NOT EXISTS requests (
                                     id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                     description VARCHAR(255) NOT NULL,
                                     requester_id BIGINT,
                                     created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                                     CONSTRAINT pk_request PRIMARY KEY (id),
                                     CONSTRAINT fk_requests_users FOREIGN KEY (requester_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS comments (
                                     id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
                                     text VARCHAR(512) NOT NULL,
                                     item_id BIGINT NOT NULL,
                                     author_id BIGINT NOT NULL,
                                     created TIMESTAMP,
                                     CONSTRAINT pk_comment PRIMARY KEY (id),
                                     CONSTRAINT fk_comments_items FOREIGN KEY (item_id) REFERENCES items (id),
                                     CONSTRAINT fk_comments_users FOREIGN KEY (author_id) REFERENCES users(id)
);


