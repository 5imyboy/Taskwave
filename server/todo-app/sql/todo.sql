drop database if exists todo;
create database todo;
use todo;

drop table if exists task;
drop table if exists `user`;

create table `user` (
	user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email varchar(512) not null unique,
    password_hash varchar(512) not null
);

create table task (
	task_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title varchar(100) not null,
    `description` varchar(1024),
	`status` ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'),
    hours int not null default(0),
    minutes int not null default(0),
    FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE,
    -- titles only need to be unique within one user's tasks
    CONSTRAINT uq_task_user_title UNIQUE (user_id, title)
);
