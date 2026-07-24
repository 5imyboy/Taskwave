drop database if exists todo_test;
create database todo_test;
use todo_test;

drop table if exists task;
drop table if exists `user`;

create table `user` (
	user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email varchar(256) not null unique,
	password_hash varchar(512) not null
);

create table task (
	task_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title varchar(100) not null unique,
    `description` varchar(1024),
	`status` ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'),
    hours int not null default(0),
    minutes int not null default(0),
    FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE
);

delimiter //
create procedure set_good_known_state()
begin
    -- Resets the auto_increment value.
    SET FOREIGN_KEY_CHECKS = 0;
    TRUNCATE TABLE task;
    TRUNCATE TABLE `user`;
    SET FOREIGN_KEY_CHECKS = 1;

	insert into `user`
    values
		(1, "testOne@email.com", "$2a$10$dos11HQE1eDwpfBaXfmMiusFRSSPOrYZejckqkV85pgItNsrbn59a"), ## hash for 'password'
        (2, "testTwo@email.com", "$2a$10$CgwtnVZvMAMA4zXiJ.B8a.fogAnhIYepMc/zxsJj7Z0mBDEMfG9fS"); ## hash for 'password2'

    insert into task
    values
		(1, 1, "Clean Room", "Make the bed and vacumm the floor", 'NOT_STARTED', 2, 0),
		(2, 1, "Brush Teeth", "Brush for at least 3 minutes!", 'IN_PROGRESS', 0, 10),
		(3, 1, "Wake Up", "", 'COMPLETED', 0, 5);
end //
delimiter ;