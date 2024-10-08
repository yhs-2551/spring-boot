create table tbl_reply (
    rno number(10, 0),
    bno number(10, 0),
    reply varchar2(1000) not null,
    replyer varchar2(50) not null,
    replyDate date default sysdate,
    updateDate date default sysdate
);

DROP SEQUENCE seq_reply;
create sequence seq_reply;

alter table tbl_reply add constraint pk_reply primary key (rno);
alter table tbl_reply add constraint fk_reply_board foreign key (bno) references tbl_board (bno);
