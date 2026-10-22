INSERT INTO cars (brand, model, image_url, description) VALUES
('Toyota', 'Corolla', 'https://upload.wikimedia.org/wikipedia/commons/d/d2/2020_Toyota_Corolla_SE%2C_front_2.29.20.jpg', 'Reliable and fuel-efficient sedan'),
('Honda', 'Civic', 'https://production.autoforce.com/uploads/version/profile_image/2701/comprar-exl_414646a7fc.png', 'Sporty and compact sedan'),
('Ford', 'Mustang', 'https://upload.wikimedia.org/wikipedia/commons/5/5d/2018_Ford_Mustang_GT_5.0_Front.jpg', 'Iconic American muscle car'),
('Chevrolet', 'Camaro', 'https://www.riocarchevroletpetrolina.com.br/content/dam/chevrolet/sa/br/pt/master/home/performance/camaro/colorizer/chevrolet-camaro-coupe-laranja-sunset.png?imwidth=900', 'High-performance sports car'),
('Tesla', 'Model S', 'https://www.tesla.com/ownersmanual/images/GUID-752B0ED4-D0A1-46C4-A141-F6A339F786A3-online-en-US.png', 'Luxurious electric sedan with autopilot'),
('BMW', 'X5', 'https://grandbrasil.com.br/wp-content/uploads/2020/02/5c1b0-grupo-de-mc3a1scara-1.webp', 'Premium mid-size SUV with advanced features');

INSERT INTO users (name, username, password, role) VALUES
('Alice Smith', 'alice.smith', '$2a$12$EeR8uG/7vAK8w36C2jtNJ.SZ7r8oEbUZMD.VyfFqb1v15oJRsG0U2', 'ROLE_ADMIN'),
('Bob Johnson', 'bob.johnson', '$2a$12$I/acKmHHTFi5NIr9Nz.MduBemaaTgJg3oHtVdPYKjXXXuIXPEjy7m', 'ROLE_USER'),
('Charlie Brown', 'charlie.brown', '$2a$12$RejdysWtClnBHeIS9ZIb8OZFGQDdbSOvG7HOIuLja1nf3uOiW3t7i', 'ROLE_USER');
