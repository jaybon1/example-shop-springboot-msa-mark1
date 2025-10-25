package com.example.shop.user.presentation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = UserControllerV1.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.config.import=optional:classpath:/"
})
@AutoConfigureRestDocs
class UserControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("유저 목록 조회 시 더미 데이터가 반환된다")
    void getUsers_returnsDummyUsers() throws Exception {
        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/v1/users")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.users", hasSize(2)))
                .andExpect(jsonPath("$.data.users[0].username", equalTo("admin")))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "user-get-users",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("User V1")
                                                .summary("사용자 목록 조회")
                                                .description("등록된 사용자 목록을 조회합니다.")
                                                .build()
                                )
                        )
                );
    }

    @Test
    @DisplayName("유저 단건 조회 시 요청 ID가 응답에 포함된다")
    void getUser_returnsRequestedId() throws Exception {
        UUID userId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/v1/users/{id}", userId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.id", equalTo(userId.toString())))
                .andExpect(jsonPath("$.data.user.nickname", equalTo("더미 유저")))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "user-get-user",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("User V1")
                                                .summary("사용자 단건 조회")
                                                .description("지정한 사용자 ID로 단일 사용자를 조회합니다.")
                                                .pathParameters(
                                                        ResourceDocumentation.parameterWithName("id")
                                                                .type(SimpleType.STRING)
                                                                .description("조회할 사용자 ID")
                                                )
                                                .build()
                                )
                        )
                );
    }

    @Test
    @DisplayName("유저 삭제 시 성공 메시지를 반환한다")
    void deleteUser_returnsSuccessMessage() throws Exception {
        UUID userId = UUID.fromString("44444444-4444-4444-4444-444444444444");

        mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/v1/users/{id}", userId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo(userId + " 사용자가 삭제되었습니다.")))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "user-delete-user",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("User V1")
                                                .summary("사용자 삭제")
                                                .description("지정한 사용자 ID로 사용자를 삭제합니다.")
                                                .pathParameters(
                                                        ResourceDocumentation.parameterWithName("id")
                                                                .type(SimpleType.STRING)
                                                                .description("삭제할 사용자 ID")
                                                )
                                                .build()
                                )
                        )
                );
    }
}
