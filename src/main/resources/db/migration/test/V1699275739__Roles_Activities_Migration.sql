create table roles
(
    role varchar(48) not null primary key
);
create table activities
(
    activity varchar(128) not null primary key
);

create table roles_activities
(
    role     varchar(48)  not null,
    activity varchar(128) not null,
    primary key (role, activity)
);

create table credentials_roles
(
    user_name varchar(48) not null,
    role      varchar(48) not null,
    primary key (user_name, role)
);