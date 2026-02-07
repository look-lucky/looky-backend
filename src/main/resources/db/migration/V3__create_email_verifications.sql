create table email_verifications (
    id bigint auto_increment primary key,
    email varchar(320) not null,
    code varchar(10) not null,
    expires_at datetime(6) not null,
    verified bit not null,
    verified_expires_at datetime(6),
    constraint uk_email_verifications_email unique (email)
);

