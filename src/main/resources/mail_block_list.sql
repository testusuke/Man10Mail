create table mail_block_list
(
	id int auto_increment,
	to_player VARCHAR(36) not null,
	from_player VARCHAR(36) not null,
	constraint mail_block_list_pk
		primary key (id)
);