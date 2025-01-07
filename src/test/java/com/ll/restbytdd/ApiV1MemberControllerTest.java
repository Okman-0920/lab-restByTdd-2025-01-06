package com.ll.restbytdd;

import com.ll.restbytdd.domain.member.member.controller.ApiV1MemberController;
import com.ll.restbytdd.domain.member.member.entity.Member;
import com.ll.restbytdd.domain.member.member.service.MemberService;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1MemberControllerTest {
    @Autowired // 테스트는 의존성 주입을 Autowired로 해야 함
    private MemberService memberService;

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
                                // 내용은 J
                                .content("""
                                    {
                                        "username": "usernew"
                                        "password": "1234"
                                        "nickname": "무명"
                                    }
                                    """.stripIndent())
                                .contentType( // Content Type는 JSON 이다, 언어 포멧은 UTF_8 이다.
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                // 요청과 응답 내용을 콘솔에 출력
                .andDo(print());


        resultActions // 테스트의 결과는 다음과 같기를 기대한다.
                // 요청을 처리한 컨트롤러가 AVMContoller 이어야 한다.
                .andExpect(handler().handlerType(ApiV1MemberController.class))
                // "join" 메서드가 실행되어야 한다.
                .andExpect(handler().methodName("join"))
                // HTTP 응답 상태 코드가 201(CREATED)이어야 한다.
                .andExpect(status().isCreated())
                // json이 처리되면 resultcode에는 201-1이어야 한다.
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                // mag는 존재해야하고, 아래와 같이 출력되어야 한다.
                .andExpect(jsonPath("$.msg").value("무명님 환영합니다."))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.createDate").isString())
                .andExpect(jsonPath("$.data.modifyDate").isString())
                .andExpect(jsonPath("$.data.nickname").value("무명"));

        Member member = memberService.findByUsername("usernew").get();
        assertThat(member.getNickname()).isEqualTo("무명");
    }
}
