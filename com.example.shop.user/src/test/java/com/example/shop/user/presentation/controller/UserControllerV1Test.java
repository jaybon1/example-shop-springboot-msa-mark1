package com.example.shop.user.presentation.controller;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.example.shop.user.application.service.UserServiceV1;
import com.example.shop.user.application.cache.AuthCache;
import com.example.shop.user.infrastructure.security.jwt.JwtProperties;
import com.example.shop.user.presentation.dto.response.ResGetUserDtoV1;
import com.example.shop.user.presentation.dto.response.ResGetUsersDtoV1;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserControllerV1.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.config.import=optional:classpath:/"
})
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
@Import(JwtProperties.class)
class UserControllerV1Test {

    private static final String DUMMY_BEARER_TOKEN = "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserServiceV1 userServiceV1;

    @MockitoBean
    private AuthCache authCache;

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry registry) {
        registry.add("shop.security.jwt.secret", () -> "testsalt");
        registry.add("shop.security.jwt.access-expiration-millis", () -> 1_800_000L);
        registry.add("shop.security.jwt.refresh-expiration-millis", () -> 15_552_000_000L);
        registry.add("shop.security.jwt.access-header-name", () -> "Authorization");
        registry.add("shop.security.jwt.header-prefix", () -> "Bearer ");
        registry.add("shop.security.jwt.access-subject", () -> "accessJwt");
        registry.add("shop.security.jwt.refresh-subject", () -> "refreshJwt");
    }

    @Test
    @DisplayName("유저 목록 조회 시 더미 데이터가 반환된다")
    void getUsers_returnsDummyUsers() throws Exception {
        ResGetUsersDtoV1 response = ResGetUsersDtoV1.builder()
                .userPage(
                        new ResGetUsersDtoV1.UserPageDto(
                                ResGetUsersDtoV1.UserPageDto.UserDto.builder()
                                        .id("11111111-1111-1111-1111-111111111111")
                                        .username("admin")
                                        .nickname("관리자")
                                        .email("admin@example.com")
                                        .build(),
                                ResGetUsersDtoV1.UserPageDto.UserDto.builder()
                                        .id("22222222-2222-2222-2222-222222222222")
                                        .username("user1")
                                        .nickname("사용자1")
                                        .email("user1@example.com")
                                        .build()
                        )
                )
                .build();
        given(userServiceV1.getUsers(any(), anyList(), any(Pageable.class), nullable(String.class), nullable(String.class), nullable(String.class)))
                .willReturn(response);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/v1/users")
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userPage.content", hasSize(2)))
                .andExpect(jsonPath("$.data.userPage.content[0].username", equalTo("admin")))
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
        ResGetUserDtoV1 response = ResGetUserDtoV1.builder()
                .user(
                        ResGetUserDtoV1.UserDto.builder()
                                .id(userId.toString())
                                .username("dummy-user")
                                .nickname("더미 유저")
                                .email("dummy@example.com")
                                .build()
                )
                .build();
        given(userServiceV1.getUser(any(), anyList(), eq(userId))).willReturn(response);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/v1/users/{id}", userId)
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
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
        willDoNothing().given(userServiceV1).deleteUser(any(), anyList(), eq(userId));

        mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/v1/users/{id}", userId)
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
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
