CREATE TABLE IF NOT EXISTS  ip(
    ip varchar(32) not null,
    port int not null,
    PRIMARY KEY (ip,port)
)