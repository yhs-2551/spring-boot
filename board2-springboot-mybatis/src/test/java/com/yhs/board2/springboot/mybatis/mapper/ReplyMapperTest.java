package com.yhs.board2.springboot.mybatis.mapper;

import com.yhs.board2.springboot.mybatis.domain.Criteria;
import com.yhs.board2.springboot.mybatis.domain.ReplyDTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;

@Log4j2
@SpringBootTest
public class ReplyMapperTest {
    @Autowired
    private ReplyMapper replyMapper;

    private Long[] bnoArr = {354861L, 354862L, 354863L, 354864L, 354865L};

    @Test
    public void testMapper() {
        log.info(replyMapper);
    }

    @Test
    public  void testCreate() {
        IntStream.rangeClosed(0, 9).forEach(i -> {
            ReplyDTO replyDTO = new ReplyDTO();

            replyDTO.setBno(bnoArr[i % 5]);
            replyDTO.setReply("댓글 테스트" + i);
            replyDTO.setReplyer("replyer" + i);

            replyMapper.insert(replyDTO);
        });
    }

    @Test
    public void testRead() {
        Long targetRno = 5L;
        ReplyDTO replyDTO = replyMapper.read(targetRno);

        log.info("ReplyDTO >>> " + replyDTO);
    }

    @Test
    public void testDelete() {
        Long targetRno = 5L;
        int count = replyMapper.delete(targetRno);

        log.info("삭제 성공 >>> " + count);
    }

    @Test
    public void testUpdate() {

        Long targetBno = 7L;

        ReplyDTO replyDTO = replyMapper.read(targetBno);
        replyDTO.setReply("내용 수정 테스트");
        int count = replyMapper.update(replyDTO);
        log.info("삭제 성공 >>> " + count);
    }

    @Test
    public void testList() {
        Criteria criteria = new Criteria();
        List<ReplyDTO> replyDTOList = replyMapper.getListWithPaging(criteria, bnoArr[0]);

        replyDTOList.forEach(reply -> log.info("댓글 리스트 테스트1>>>" + reply));

    }

    @Test
    public void testList2() {
        Criteria cri = new Criteria(2, 10);
        List<ReplyDTO> replyDTOList = replyMapper.getListWithPaging(cri, bnoArr[0]);

        replyDTOList.forEach(reply -> log.info("댓글 리스트 테스트2 >>>" + reply));

    }

    @Test
    public void testCount() {
         log.info("특정 게시물 댓글 총 개수 파악 >>> " + replyMapper.getCountByBno(bnoArr[0]));

    }
}
