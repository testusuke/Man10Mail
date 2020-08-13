create table mail_list
(
    id int auto_increment,
    to_player varchar(36) not null,
    to_name varchar(16) not null,
    from_player varchar(36) not null,
    from_name varchar(16) not null,
    title varchar(40) default 'none title' null,
    message text null,
    tag varchar(40) default 'none' null,
    date datetime null,
    `read` boolean default false null,
    constraint mail_list_pk
        primary key (id)
);

create index mail_list_to_index
    on mail_list (to_player);