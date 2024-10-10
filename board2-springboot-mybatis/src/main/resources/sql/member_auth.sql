create table tbl_member_auth(
userid varchar2(50) not null,
auth varchar2(50) not null,
CONSTRAINT fk_member_auth FOREIGN KEY(userid) REFERENCES tbl_member(userid)
);