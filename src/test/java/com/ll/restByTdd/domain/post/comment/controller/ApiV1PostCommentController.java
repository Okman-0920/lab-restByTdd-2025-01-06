package com.ll.restByTdd.domain.post.comment.controller;

import com.ll.restByTdd.domain.member.member.service.MemberService;
import com.ll.restByTdd.domain.post.comment.entity.PostComment;
import com.ll.restByTdd.domain.post.post.entity.Post;
import com.ll.restByTdd.domain.post.post.service.PostService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiV1PostCommentController {
    @Autowired
    private PostService postService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("다건 조회")
    void t1() throws Exception {
//        Member actor = memberService.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/{postId}/commments")
//                                .header("Authorization", "Bearer " + actor.getApiKey())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("items"))
                .andExpect(status().isOk())

        List<PostComment> comments = postService.findById(1).get().getComments();

        for (int i = 0; i < comments.size(); i++) {
            PostComment postComment = comments.get(i);
            resultActions
                    .andExpect(jsonPath("$.items[%d].id".formatted(i)).value(postComment.getId()))
                    .andExpect(jsonPath("$.items[%d].createDate".formatted(i)).value(Matchers.startsWith(postComment.getCreateDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$.items[%d].modifyDate".formatted(i)).value(Matchers.startsWith(postComment.getModifyDate().toString().substring(0, 25))))
                    .andExpect(jsonPath("$.items[%d].authorId".formatted(i)).value(postComment.getAuthor().getId()))
                    .andExpect(jsonPath("$.items[%d].authorName".formatted(i)).value(postComment.getAuthor().getName()))
                    .andExpect(jsonPath("$.items[%d].content".formatted(i)).value(postComment.getContent()));
        }
    }
}
