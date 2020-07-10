create table mail_read
(
	id int auto_increment,
	to_player varchar(36) not null,
	from_mail_id int null,
	constraint mail_read_pk
		primary key (id)
);