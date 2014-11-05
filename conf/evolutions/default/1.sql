# --- !Ups

create table Song (
    id serial NOT NULL,
    name varchar(255) NOT NULL,
    primary key (id)
);

create table Assignment(
    id serial NOT NULL,
    song_id integer NOT NULL,
    rank integer NOT NULL,
    content varchar(1024) NOT NULL,
    pre varchar(1024),
    post varchar(1024),
    spots integer NOT NULL,
    primary key (id),
    foreign key (song_id) references Song(id)

);

create table Engagement(
    id serial NOT NULL,
    email varchar(255),
    assignment_id integer,
    completed boolean,
    primary key(id),
    foreign key(assignment_id) references Assignment(id),
    unique(email, assignment_id)
);

create table AllowedEmail(
    email varchar(255) primary key
);

# --- !Downs

drop table Engagement;
drop table Assignment;
drop table Song;
