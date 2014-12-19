# --- !Ups
CREATE TABLE Achievement(
  id serial NOT NULL,
  assignment_id integer NOT NULL,
  file_path varchar(256),
  PRIMARY KEY (id),
  FOREIGN KEY (assignment_id) REFERENCES Assignment(id)
);


# --- !Downs

DROP TABLE Achievement;