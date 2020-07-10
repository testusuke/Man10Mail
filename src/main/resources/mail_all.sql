create table mail_all
(
	id int auto_increment,
	from_name varchar(36) not null,
	title varchar(40) default 'nore' null,
	message text null,
	tag varchar(40) default 'none' null,
	date datetime null,
	constraint mail_all_pk
		primary key (id)
);