CREATE DATABASE IF NOT EXISTS healthshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE healthshop;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(255) NOT NULL UNIQUE,
  phone VARCHAR(255) UNIQUE,
  email VARCHAR(255) UNIQUE,
  password VARCHAR(255) NOT NULL,
  role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
  enabled BOOLEAN DEFAULT TRUE,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS products (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  image_url VARCHAR(255),
  price DECIMAL(10, 2) NOT NULL,
  tags VARCHAR(500),
  target_audience VARCHAR(500),
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_profiles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  age_group VARCHAR(50) NOT NULL,
  gender VARCHAR(50) NOT NULL,
  health_goals VARCHAR(500),
  budget VARCHAR(50) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cart_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(255) NOT NULL,
  product_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  PRIMARY KEY (id)
);

INSERT INTO products (name, description, image_url, price, tags, target_audience) VALUES
('深海鱼油软胶囊', '高纯度Omega-3，辅助调节血脂，维护心血管健康。', 'https://placehold.co/600x400/2980b9/ffffff?text=Fish+Oil', 199.00, 'cardio,nutrition', 'all,31-50,50+'),
('辅酶Q10', '心脏动力来源，增强心肌活力，抗氧化。', 'https://placehold.co/600x400/c0392b/ffffff?text=CoQ10', 259.00, 'cardio,energy', 'all,31-50,50+'),
('叶黄素', '缓解眼疲劳，过滤蓝光，适合长期用眼人群。', 'https://placehold.co/600x400/8e44ad/ffffff?text=Lutein', 89.00, 'eyes,sleep', 'all,18-30,31-50'),
('乳清蛋白粉', '快速补充蛋白质，促进肌肉修复与生长。', 'https://placehold.co/600x400/27ae60/ffffff?text=Whey+Protein', 328.00, 'fitness,protein,energy', 'all,male,18-30,31-50'),
('左旋肉碱', '配合运动加速脂肪燃烧，提升耐力。', 'https://placehold.co/600x400/e67e22/ffffff?text=L-Carnitine', 158.00, 'fitness,energy', 'all,male,female,18-30,31-50'),
('多维元素片（男士）', '专为男士设计，补充日常维生素和矿物质。', 'https://placehold.co/600x400/f1c40f/333333?text=Multivitamin', 119.00, 'nutrition,energy', 'male,31-50,50+');
