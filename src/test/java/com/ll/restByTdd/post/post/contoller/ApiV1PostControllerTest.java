package com.ll.restByTdd.post.post.contoller;

import com.ll.restByTdd.domain.member.member.entity.Member;
import com.ll.restByTdd.domain.member.member.service.MemberService;
import com.ll.restByTdd.domain.post.post.controller.ApiV1PostController;
import com.ll.restByTdd.domain.post.post.entity.Post;
import com.ll.restByTdd.domain.post.post.service.PostService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1PostControllerTest {
    @Autowired // 테스트는 의존성 주입을 Autowired 를 사용하여 강제로 해야 함
    private MemberService memberService;

    @Autowired
    private PostService postService;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("1번글 조회")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/1")
                )
                .andDo(print());

        Post post = postService.findById(1).get();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0,25))))
                .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(post.getModifyDate().toString().substring(0,25))))
                .andExpect(jsonPath("$.authorId").value(post.getAuthor().getId()))
                .andExpect(jsonPath("$.authorName").value(post.getAuthor().getName()))
                .andExpect(jsonPath("$.title").value(post.getTitle()))
                .andExpect(jsonPath("$.content").value(post.getContent()));
    }

    @Test
    @DisplayName("존재하지 않는 글 조회")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/10")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("item"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("글 작성")
    void t3() throws Exception {
        Member actor = memberService.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/write")
                                // header: HTTP 요청 헤더(header)에 특정 값을 추가
                                // "Authorization": 헤더의 이름으로, 클라이언트가 서버에 인증 정보를 전달하기 위해 사용
                                // "Bearer " + actor.getApiKey(): 헤더 값으로 "Bearer "라는 인증 유형과 함께 API 키를 추가
                                // "Bearer ": 인증 방식 중 하나로, 토큰을 사용하는 방식
                                .header("Authorization", "Bearer " + actor.getApiKey())
                                .content("""
                                        {
                                            "title": "글1",
                                            "content": "글1의 내용"
                                        }
                                        """)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                )
                .andDo(print());

        Post post = postService.findLaTest().get();

        assertThat(post.getAuthor()).isEqualTo(actor);

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("writeItem"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글이 작성되었습니다".formatted(post.getId())))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0,25))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(post.getModifyDate().toString().substring(0,25))))
                .andExpect(jsonPath("$.data.authorId").value(post.getAuthor().getId()))
                .andExpect(jsonPath("$.data.authorName").value(post.getAuthor().getName()))
                .andExpect(jsonPath("$.data.title").value(post.getTitle()))
                .andExpect(jsonPath("$.data.content").value(post.getContent()));

    }

    @Test
    @DisplayName("글 작성, with no actor")
    void t4() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/api/v1/posts/write")
                                .content("""
                                        {
                                            "title": "글1",
                                            "content": "글1의 내용"
                                        }
                                        """)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("writeItem"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("apiKey를 입력해주세요."));
    }

    @Test
    @DisplayName("글 수정")
    void t5() throws Exception {
        Member actor = memberService.findByUsername("user1").get();
        Post post = postService.findById(1).get();

        LocalDateTime oldModifyDate = post.getModifyDate();

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/1")
                                .header("Authorization", "Bearer " + actor.getApiKey())
                                .content("""
                                        {
                                            "title" : "글1의 수정 제목",
                                            "content": "글1의 수정 내용"
                                        }
                                        """)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modifyItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글 수정이 완료되었습니다".formatted(post.getId())))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0,25))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.not(Matchers.startsWith(oldModifyDate.toString().substring(0,25)))))
                .andExpect(jsonPath("$.data.authorId").value(post.getAuthor().getId()))
                .andExpect(jsonPath("$.data.authorName").value(post.getAuthor().getName()))
                .andExpect(jsonPath("$.data.title").value(post.getTitle()))
                .andExpect(jsonPath("$.data.content").value(post.getContent()));
    }

    @Test
    @DisplayName("글 수정, with no actor")
    void t6() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/1")
                                .content("""
                                        {
                                            "title" : "글1의 수정 제목",
                                            "content": "글1의 수정 내용"
                                        }
                                        """)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modifyItem"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("apiKey를 입력해주세요."));
    }

    @Test
    @DisplayName("글 수정, with no permission")
    void t7() throws Exception {
        Member actor = memberService.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/3")
                                .header("Authorization", "Bearer " + actor.getApiKey())
                                .content("""
                                        {
                                            "title" : "글1의 수정 제목",
                                            "content": "글1의 수정 내용"
                                        }
                                        """)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("modifyItem"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("작성자만 글을 수정할 수 있습니다."));
    }

    @Test
    @DisplayName("글 삭제")
    void t8() throws Exception {
        Member actor = memberService.findByUsername("user1").get();

        Post post = postService.findById(1).get();

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/1")
                                .header("Authorization", "Bearer " + actor.getApiKey())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("deleteItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 글이 삭제되었습니다.".formatted(post.getId())));

        assertThat(postService.findById(1)).isEmpty();
    }

    @Test
    @DisplayName("글 삭제, with not existing post id")
    void t9() throws Exception {
        Member actor = memberService.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/10")
                                .header("Authorization", "Bearer " + actor.getApiKey())
                )
                .andDo(print());

        assertThat(postService.findById(10)).isEmpty();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("deleteItem"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("글 삭제, no actor")
    void t10() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/1")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("deleteItem"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("apiKey를 입력해주세요."));
    }

    @Test
    @DisplayName("글 삭제, with no permission")
    void t11() throws Exception {
        Member actor = memberService.findByUsername("user1").get();

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/3")
                                .header("Authorization", "Bearer " + actor.getApiKey())
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("deleteItem"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.resultCode").value("403-1"))
                .andExpect(jsonPath("$.msg").value("작성자만 글을 삭제할 수 있습니다."));
    }

    @Test
    @DisplayName("비공개 글 6번 조회, with 작성자")
    void t12() throws Exception {
        Member actor = memberService.findByUsername("user4").get();

        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/6")
                                .header("Authorization", "Bearer " + actor.getApiKey())
                )
                .andDo(print());

        Post post = postService.findById(6).get();

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(post.getCreateDate().toString().substring(0,25))))
                .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(post.getModifyDate().toString().substring(0,25))))
                .andExpect(jsonPath("$.authorId").value(post.getAuthor().getId()))
                .andExpect(jsonPath("$.authorName").value(post.getAuthor().getName()))
                .andExpect(jsonPath("$.title").value(post.getTitle()))
                .andExpect(jsonPath("$.content").value(post.getContent()));
    }

    @Test
    @DisplayName("비공개 글 6번 조회, with no actor")
    void t13() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/6")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(ApiV1PostController.class))
                .andExpect(handler().methodName("item"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value("401-1"))
                .andExpect(jsonPath("$.msg").value("apiKey를 입력해주세요."));
    }
}