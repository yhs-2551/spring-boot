-- board_table
 drop table if exists board_table;
 create table board_table
 (
	id bigint primary key auto_increment,
    boardTitle varchar(50),
    boardWriter varchar(20),
    boardPassword varchar(20),
    boardContents varchar(500),
    boardHits int default 0,
    createdAt datetime default now(),
    fileAttached int default 0
);
-- board_file_table
drop table if exists board_file_table;
create table board_file_table
(
    id bigint auto_increment primary key,
    originalFileName varchar(100),
    storedFileName varchar(100),
    boardId bigint,
    constraint fk_board_file foreign key(boardId) references board_table(id) on delete cascade
);

DROP TABLE IF EXISTS board_comment_table;
CREATE TABLE board_comment_table
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,           -- 댓글 ID
    boardId BIGINT NOT NULL,                        -- 댓글이 달린 게시글 ID
    commentWriter VARCHAR(50),                      -- 댓글 작성자
    commentContents VARCHAR(500),                   -- 댓글 내용
    createdAt DATETIME DEFAULT NOW(),               -- 댓글 작성 일시
    parentCommentId BIGINT DEFAULT NULL,            -- 대댓글의 상위 댓글 ID (NULL이면 일반 댓글)
    CONSTRAINT fk_board_comment FOREIGN KEY (boardId) REFERENCES board_table(id) ON DELETE CASCADE,
    CONSTRAINT fk_parent_comment FOREIGN KEY (parentCommentId) REFERENCES board_comment_table(id) ON DELETE CASCADE
);

