# picture_properties schema

# --- !Ups

create table picture_properties(
  picture_id bigint(20) not null auto_increment,
  status varchar(255) not null,
  twitter_id bigint(20) not null,
  file_name varchar(255) not null,
  content_type varchar(255) not null,
  overlay_text varchar(255) not null,
  overlay_text_size int(4) not null,
  original_filepath varchar(255) not null,
  converted_filepath varchar(255),
  created_time datetime not null,
  primary key (picture_id),
  index(twitter_id),
  index(created_time)
) engine=innodb charset=utf8mb4;

# --- !Downs

drop table picture_properties;