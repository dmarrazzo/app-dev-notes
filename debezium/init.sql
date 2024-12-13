CREATE TABLE inventory (
    item_no      INT             NOT NULL,  -- UNSIGNED AUTO_INCREMENT??
    added  DATE            NOT NULL,
    name  VARCHAR(14)     NOT NULL,
    descr   VARCHAR(50)     NOT NULL,
    PRIMARY KEY (item_no)
);

insert INTO inventory (item_no, added, name, descr) values ( 1, CURDATE(), 'clock', 'wall clock');
