# --- !Ups

alter table Song add column video_link VARCHAR(512);
alter table Assignment add column start_time VARCHAR(32);


# --- !Downs

alter table Song DROP COLUMN video_link;
alter table Assignment drop column start_time;

