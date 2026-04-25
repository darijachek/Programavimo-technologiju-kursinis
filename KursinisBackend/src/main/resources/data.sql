DELETE FROM message;
DELETE FROM review;
DELETE FROM order_item;
DELETE FROM orders;
DELETE FROM menu_item;
DELETE FROM restaurant;

DELETE FROM admin;
DELETE FROM driver;
DELETE FROM owner;
DELETE FROM client;
DELETE FROM users;

ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE restaurant ALTER COLUMN id RESTART WITH 1;
ALTER TABLE menu_item ALTER COLUMN id RESTART WITH 1;
ALTER TABLE orders ALTER COLUMN id RESTART WITH 1;
ALTER TABLE order_item ALTER COLUMN id RESTART WITH 1;
ALTER TABLE message ALTER COLUMN id RESTART WITH 1;
ALTER TABLE review ALTER COLUMN id RESTART WITH 1;

-- USERS

-- Clients (slaptažodis '1234')
INSERT INTO users (id, username, password_hash, role) VALUES (1, 'client_ana', '$2a$12$piC8Ww4mS3ajNiGjnnwUK.QrcSODdScN8xa9uaBgpsGT9jxqWX4uK', 'CLIENT');
INSERT INTO client (id, address, loyalty_points) VALUES (1, 'Vilnius, Šeimyniškių g. 5', 35);

INSERT INTO users (id, username, password_hash, role) VALUES (2, 'client_mantas', '$2a$12$piC8Ww4mS3ajNiGjnnwUK.QrcSODdScN8xa9uaBgpsGT9jxqWX4uK', 'CLIENT');
INSERT INTO client (id, address, loyalty_points) VALUES (2, 'Kaunas, Laisvės al. 18', 12);

INSERT INTO users (id, username, password_hash, role) VALUES (3, 'client_ugne', '$2a$12$piC8Ww4mS3ajNiGjnnwUK.QrcSODdScN8xa9uaBgpsGT9jxqWX4uK', 'CLIENT');
INSERT INTO client (id, address, loyalty_points) VALUES (3, 'Klaipėda, Taikos pr. 61', 4);

INSERT INTO users (id, username, password_hash, role) VALUES (4, 'client_tomas', '$2a$12$piC8Ww4mS3ajNiGjnnwUK.QrcSODdScN8xa9uaBgpsGT9jxqWX4uK', 'CLIENT');
INSERT INTO client (id, address, loyalty_points) VALUES (4, 'Vilnius, Ozo g. 25', 0);

-- Drivers (slaptažodis '1234')
INSERT INTO users (id, username, password_hash, role) VALUES (5, 'driver_karolis', '$2a$12$piC8Ww4mS3ajNiGjnnwUK.QrcSODdScN8xa9uaBgpsGT9jxqWX4uK', 'DRIVER');
INSERT INTO driver (id, vehicle, rating) VALUES (5, 'Toyota Prius (white)', 4.8);

INSERT INTO users (id, username, password_hash, role) VALUES (6, 'driver_eva', '$2a$12$piC8Ww4mS3ajNiGjnnwUK.QrcSODdScN8xa9uaBgpsGT9jxqWX4uK', 'DRIVER');
INSERT INTO driver (id, vehicle, rating) VALUES (6, 'VW Golf (black)', 4.9);

INSERT INTO users (id, username, password_hash, role) VALUES (7, 'driver_povilas', '$2a$12$piC8Ww4mS3ajNiGjnnwUK.QrcSODdScN8xa9uaBgpsGT9jxqWX4uK', 'DRIVER');
INSERT INTO driver (id, vehicle, rating) VALUES (7, 'Bolt scooter', 4.6);

-- Owners (slaptažodis '1234')
INSERT INTO users (id, username, password_hash, role) VALUES (8, 'owner_pizza', '$2a$12$piC8Ww4mS3ajNiGjnnwUK.QrcSODdScN8xa9uaBgpsGT9jxqWX4uK', 'OWNER');
INSERT INTO owner (id) VALUES (8);

INSERT INTO users (id, username, password_hash, role) VALUES (9, 'owner_sushi', '$2a$12$piC8Ww4mS3ajNiGjnnwUK.QrcSODdScN8xa9uaBgpsGT9jxqWX4uK', 'OWNER');
INSERT INTO owner (id) VALUES (9);

INSERT INTO users (id, username, password_hash, role) VALUES (10, 'owner_burger', '$2a$12$piC8Ww4mS3ajNiGjnnwUK.QrcSODdScN8xa9uaBgpsGT9jxqWX4uK', 'OWNER');
INSERT INTO owner (id) VALUES (10);

-- Admin (slaptažodis 'admin')
INSERT INTO users (id, username, password_hash, role) VALUES (11, 'admin', '$2a$12$JRE7PHlcJtF8qM2yG7CyBuBR2kUtHLZwibC.ur/RIL.xkdO7Wr.uq', 'ADMIN');
INSERT INTO admin (id) VALUES (11);

-- RESTAURANTS
INSERT INTO restaurant (id, name, address, phone, active, owner_id)
VALUES (1, 'Pilies Pica', 'Vilnius, Pilies g. 12', '+37060010001', TRUE, 8);

INSERT INTO restaurant (id, name, address, phone, active, owner_id)
VALUES (2, 'Sushi Nami', 'Kaunas, Savanorių pr. 82', '+37060010002', TRUE, 9);

INSERT INTO restaurant (id, name, address, phone, active, owner_id)
VALUES (3, 'Burgerių Kampas', 'Klaipėda, H. Manto g. 7', '+37060010003', TRUE, 10);

-- MENU ITEMS

-- Pilies Pica (restaurant 1)
INSERT INTO menu_item (id, title, description, category, base_price, restaurant_id)
VALUES (1, 'Margherita', 'Pomidorų padažas, mocarela, bazilikas', 'PIZZA', 8.50, 1);

INSERT INTO menu_item (id, title, description, category, base_price, restaurant_id)
VALUES (2, 'Pepperoni', 'Pepperoni, mocarela, aštresnis padažas', 'PIZZA', 9.90, 1);

INSERT INTO menu_item (id, title, description, category, base_price, restaurant_id)
VALUES (3, '4 Sūriai', 'Mocarela, gorgonzola, parmezanas, edamas', 'PIZZA', 10.20, 1);

INSERT INTO menu_item (id, title, description, category, base_price, restaurant_id)
VALUES (4, 'Cezario salotos', 'Vištiena, salotos, parmezanas, padažas', 'SALAD', 6.80, 1);

-- Sushi Nami (restaurant 2)
INSERT INTO menu_item (id, title, description, category, base_price, restaurant_id)
VALUES (5, 'Miso sriuba', 'Klasikinė miso sriuba', 'SOUP', 2.90, 2);

INSERT INTO menu_item (id, title, description, category, base_price, restaurant_id)
VALUES (6, 'California roll', 'Krabas, avokadas, agurkas (8 vnt.)', 'SUSHI', 7.40, 2);

INSERT INTO menu_item (id, title, description, category, base_price, restaurant_id)
VALUES (7, 'Salmon nigiri', 'Lašiša (6 vnt.)', 'SUSHI', 8.90, 2);

INSERT INTO menu_item (id, title, description, category, base_price, restaurant_id)
VALUES (8, 'Ginger ale', 'Gėrimas 0.33L', 'DRINK', 2.20, 2);

-- Burgerių Kampas (restaurant 3)
INSERT INTO menu_item (id, title, description, category, base_price, restaurant_id)
VALUES (9, 'Classic burger', 'Jautiena, sūris, padažas, daržovės', 'BURGER', 7.90, 3);

INSERT INTO menu_item (id, title, description, category, base_price, restaurant_id)
VALUES (10, 'Chicken burger', 'Vištiena, salotos, aioli', 'BURGER', 7.30, 3);

INSERT INTO menu_item (id, title, description, category, base_price, restaurant_id)
VALUES (11, 'Bulvytės', 'Traškios bulvytės', 'SIDES', 2.80, 3);

INSERT INTO menu_item (id, title, description, category, base_price, restaurant_id)
VALUES (12, 'Cola', 'Gėrimas 0.33L', 'DRINK', 2.00, 3);

-- ORDERS

-- NEW
INSERT INTO orders (id, client_id, restaurant_id, status, created_at, driver_id)
VALUES (1, 1, 1, 'NEW', CURRENT_TIMESTAMP(), NULL);

-- ACCEPTED
INSERT INTO orders (id, client_id, restaurant_id, status, created_at, driver_id)
VALUES (2, 2, 2, 'ACCEPTED', CURRENT_TIMESTAMP(), NULL);

-- COOKING
INSERT INTO orders (id, client_id, restaurant_id, status, created_at, driver_id)
VALUES (3, 3, 3, 'COOKING', CURRENT_TIMESTAMP(), NULL);

-- READY
INSERT INTO orders (id, client_id, restaurant_id, status, created_at, driver_id)
VALUES (4, 4, 1, 'READY', CURRENT_TIMESTAMP(), NULL);

-- DELIVERING
INSERT INTO orders (id, client_id, restaurant_id, status, created_at, driver_id)
VALUES (5, 1, 2, 'DELIVERING', CURRENT_TIMESTAMP(), 6);

-- DONE
INSERT INTO orders (id, client_id, restaurant_id, status, created_at, driver_id)
VALUES (6, 2, 3, 'DONE', CURRENT_TIMESTAMP(), 5);

-- CANCELED
INSERT INTO orders (id, client_id, restaurant_id, status, created_at, driver_id)
VALUES (7, 3, 1, 'CANCELED', CURRENT_TIMESTAMP(), NULL);

-- ORDER ITEMS

-- Order 1 (NEW) - Pilies Pica
INSERT INTO order_item (id, order_id, menu_item_id, quantity) VALUES (1, 1, 1, 1);
INSERT INTO order_item (id, order_id, menu_item_id, quantity) VALUES (2, 1, 4, 1);

-- Order 2 (ACCEPTED) - Sushi
INSERT INTO order_item (id, order_id, menu_item_id, quantity) VALUES (3, 2, 6, 2);
INSERT INTO order_item (id, order_id, menu_item_id, quantity) VALUES (4, 2, 8, 2);

-- Order 3 (COOKING) - Burgers
INSERT INTO order_item (id, order_id, menu_item_id, quantity) VALUES (5, 3, 9, 1);
INSERT INTO order_item (id, order_id, menu_item_id, quantity) VALUES (6, 3, 11, 1);
INSERT INTO order_item (id, order_id, menu_item_id, quantity) VALUES (7, 3, 12, 1);

-- Order 4 (READY) - Pilies Pica (for drivers to take)
INSERT INTO order_item (id, order_id, menu_item_id, quantity) VALUES (8, 4, 2, 1);
INSERT INTO order_item (id, order_id, menu_item_id, quantity) VALUES (9, 4, 3, 1);

-- Order 5 (DELIVERING) - Sushi
INSERT INTO order_item (id, order_id, menu_item_id, quantity) VALUES (10, 5, 5, 2);
INSERT INTO order_item (id, order_id, menu_item_id, quantity) VALUES (11, 5, 7, 1);

-- Order 6 (DONE) - Burgers
INSERT INTO order_item (id, order_id, menu_item_id, quantity) VALUES (12, 6, 10, 2);
INSERT INTO order_item (id, order_id, menu_item_id, quantity) VALUES (13, 6, 11, 2);

-- Order 7 (CANCELED) - Pilies Pica
INSERT INTO order_item (id, order_id, menu_item_id, quantity) VALUES (14, 7, 1, 1);

-- MESSAGES (chat)

-- Order 5: driver (eva) -> client (ana)
INSERT INTO message (id, order_id, sender_id, recipient_id, text, created_at)
VALUES (1, 5, 6, 1, 'Sveiki! Paėmiau užsakymą, būsiu po ~10 min.', CURRENT_TIMESTAMP());

-- client -> driver
INSERT INTO message (id, order_id, sender_id, recipient_id, text, created_at)
VALUES (2, 5, 1, 6, 'Super, laukiu. Laiptinė kodas 23.', CURRENT_TIMESTAMP());

-- driver -> client (recipient may be null in your send endpoint; but DB allows it)
INSERT INTO message (id, order_id, sender_id, recipient_id, text, created_at)
VALUES (3, 5, 6, 1, 'Ačiū! Jau prie namo.', CURRENT_TIMESTAMP());


-- REVIEWS

-- Restaurant reviews
INSERT INTO review (id, author_id, restaurant_id, driver_id, client_id, target_type, rating, comment, created_at)
VALUES (1, 1, 1, NULL, NULL, 'RESTAURANT', 5, 'Puiki pica, atvyko šilta.', CURRENT_TIMESTAMP());

INSERT INTO review (id, author_id, restaurant_id, driver_id, client_id, target_type, rating, comment, created_at)
VALUES (2, 2, 2, NULL, NULL, 'RESTAURANT', 4, 'Skanu, bet norėjosi daugiau imbiero :)', CURRENT_TIMESTAMP());

INSERT INTO review (id, author_id, restaurant_id, driver_id, client_id, target_type, rating, comment, created_at)
VALUES (3, 3, 3, NULL, NULL, 'RESTAURANT', 5, 'Burgeris top! Bulvytės traškios.', CURRENT_TIMESTAMP());

-- Driver review (client -> driver)
INSERT INTO review (id, author_id, restaurant_id, driver_id, client_id, target_type, rating, comment, created_at)
VALUES (4, 1, NULL, 6, NULL, 'DRIVER', 5, 'Labai mandagi ir greita vairuotoja.', CURRENT_TIMESTAMP());

-- Client review (driver -> client)
INSERT INTO review (id, author_id, restaurant_id, driver_id, client_id, target_type, rating, comment, created_at)
VALUES (5, 5, NULL, NULL, 1, 'CLIENT', 5, 'Klientas aiškiai nurodė adresą ir atsakė greitai.', CURRENT_TIMESTAMP());

ALTER TABLE users ALTER COLUMN id RESTART WITH 12;
ALTER TABLE restaurant ALTER COLUMN id RESTART WITH 4;
ALTER TABLE menu_item ALTER COLUMN id RESTART WITH 13;
ALTER TABLE orders ALTER COLUMN id RESTART WITH 8;
ALTER TABLE order_item ALTER COLUMN id RESTART WITH 15;
ALTER TABLE message ALTER COLUMN id RESTART WITH 4;
ALTER TABLE review ALTER COLUMN id RESTART WITH 6;