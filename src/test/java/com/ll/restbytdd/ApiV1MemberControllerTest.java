package com.ll.restbytdd;

import com.ll.restbytdd.domain.member.member.controller.ApiV1MemberController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1MemberControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("회원가입")
    void t1() throws Exception {
        // 회원가입 요청
        ResultActions resultActions = mvc
                .perform(
                // HTTP POST 요청을 "/api/v1/members/join" 경로로 보냄
                        post("/api/v1/members/join")
                )
                // 요청과 응답 내용을 콘솔에 출력
                .andDo(print());


        resultActions
                // 요청을 처리한 컨트롤러가 AVMContoller 이어야 한다.
                .andExpect(handler().handlerType(ApiV1MemberController.class))

                // "join" 메서드가 실행되어야 한다.
                .andExpect(handler().methodName("join"))

                // HTTP 응답 상태 코드가 201(CREATED)이면 테스트 통과
                .andExpect(status().isCreated());
    }
}
