package com.example.shop.user.presentation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.example.shop.user.presentation.dto.request.ReqAuthPostRefreshDtoV1;
import com.example.shop.user.presentation.dto.request.ReqPostAuthAccessTokenCheckDtoV1;
import com.example.shop.user.presentation.dto.request.ReqPostAuthLoginDtoV1;
import com.example.shop.user.presentation.dto.request.ReqPostAuthRegisterDtoV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.http.MediaType;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = AuthControllerV1.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.config.import=optional:classpath:/"
})
@AutoConfigureRestDocs
class AuthControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원 가입 요청 시 성공 메시지를 반환한다")
    void register_returnsSuccessMessage() throws Exception {
        ReqPostAuthRegisterDtoV1 request = ReqPostAuthRegisterDtoV1.builder()
                .user(
                        ReqPostAuthRegisterDtoV1.UserDto.builder()
                                .username("dummy_user")
                                .password("secret1!")
                                .nickname("더미 유저")
                                .email("dummy@example.com")
                                .build()
                )
                .build();

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo("회원가입이 완료되었습니다.")))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "auth-register",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Auth V1")
                                                .summary("회원 가입")
                                                .description("신규 사용자를 등록합니다.")
                                                .build()
                                )
                        )
                );
    }

    @Test
    @DisplayName("로그인 요청 시 액세스/리프레시 토큰을 반환한다")
    void login_returnsJwtPair() throws Exception {
        ReqPostAuthLoginDtoV1 request = ReqPostAuthLoginDtoV1.builder()
                .user(
                        ReqPostAuthLoginDtoV1.UserDto.builder()
                                .username("dummy_user")
                                .password("secret1!")
                                .build()
                )
                .build();

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessJwt").exists())
                .andExpect(jsonPath("$.data.refreshJwt").exists())
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "auth-login",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Auth V1")
                                                .summary("로그인")
                                                .description("사용자 자격 증명을 기반으로 액세스/리프레시 토큰을 발급합니다.")
                                                .build()
                                )
                        )
                );
    }

    @Test
    @DisplayName("리프레시 요청 시 새로운 토큰을 반환한다")
    void refresh_returnsNewJwtPair() throws Exception {
        ReqAuthPostRefreshDtoV1 request = ReqAuthPostRefreshDtoV1.builder()
                .refreshJwt("refresh-dummy-token")
                .build();

        mockMvc.perform(post("/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessJwt").exists())
                .andExpect(jsonPath("$.data.refreshJwt").exists())
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "auth-refresh",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Auth V1")
                                                .summary("액세스 토큰 갱신")
                                                .description("유효한 리프레시 토큰으로 액세스/리프레시 토큰을 갱신합니다.")
                                                .build()
                                )
                        )
                );
    }

    @Test
    @DisplayName("액세스 토큰 검증 요청 시 더미 응답을 반환한다")
    void checkAccessToken_returnsDummyResponse() throws Exception {
        ReqPostAuthAccessTokenCheckDtoV1 request = ReqPostAuthAccessTokenCheckDtoV1.builder()
                .accessJwt("dummy.jwt.token")
                .build();

        mockMvc.perform(post("/v1/auth/access-token-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo("액세스 토큰 검증이 완료되었습니다.")))
                .andExpect(jsonPath("$.data.valid", equalTo(true)))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "auth-access-token-check",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Auth V1")
                                                .summary("액세스 토큰 검증")
                                                .description("액세스 토큰의 서명 및 만료 여부를 검증합니다.")
                                                .build()
                                )
                        )
                );
    }
}
