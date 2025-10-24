package com.example.shop.user.presentation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.example.shop.user.presentation.dto.request.ReqPostAuthAccessTokenCheckDtoV1;
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
