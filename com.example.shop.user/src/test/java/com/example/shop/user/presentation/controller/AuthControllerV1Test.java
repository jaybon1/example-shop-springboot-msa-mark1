package com.example.shop.user.presentation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.shop.user.application.service.AuthServiceV1;
import com.example.shop.user.application.cache.AuthCache;
import com.example.shop.user.infrastructure.security.auth.CustomUserDetails;
import com.example.shop.user.infrastructure.security.jwt.JwtProperties;
import com.example.shop.user.presentation.dto.request.ReqPostAuthRefreshDtoV1;
import com.example.shop.user.presentation.dto.request.ReqPostAuthCheckAccessTokenDtoV1;
import com.example.shop.user.presentation.dto.request.ReqPostAuthInvalidateBeforeTokenDtoV1;
import com.example.shop.user.presentation.dto.request.ReqPostAuthLoginDtoV1;
import com.example.shop.user.presentation.dto.request.ReqPostAuthRegisterDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthCheckAccessTokenDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthLoginDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthRefreshDtoV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = AuthControllerV1.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.config.import=optional:classpath:/"
})
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
@Import(JwtProperties.class)
class AuthControllerV1Test {

    private static final String DUMMY_BEARER_TOKEN = "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthServiceV1 authServiceV1;

    @MockitoBean
    private AuthCache authCache;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

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

        willDoNothing().given(authServiceV1).register(any(ReqPostAuthRegisterDtoV1.class));

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

        ResPostAuthLoginDtoV1 response = ResPostAuthLoginDtoV1.builder()
                .accessJwt("dummy-access-token")
                .refreshJwt("dummy-refresh-token")
                .build();
        given(authServiceV1.login(any(ReqPostAuthLoginDtoV1.class))).willReturn(response);

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
        ReqPostAuthRefreshDtoV1 request = ReqPostAuthRefreshDtoV1.builder()
                .refreshJwt("refresh-dummy-token")
                .build();

        ResPostAuthRefreshDtoV1 response = ResPostAuthRefreshDtoV1.builder()
                .accessJwt("new-access-token")
                .refreshJwt("new-refresh-token")
                .build();
        given(authServiceV1.refresh(any(ReqPostAuthRefreshDtoV1.class))).willReturn(response);

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
        ReqPostAuthCheckAccessTokenDtoV1 request = ReqPostAuthCheckAccessTokenDtoV1.builder()
                .accessJwt("dummy.jwt.token")
                .build();

        ResPostAuthCheckAccessTokenDtoV1 response = ResPostAuthCheckAccessTokenDtoV1.builder()
                .userId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .valid(true)
                .remainingSeconds(10L)
                .build();
        given(authServiceV1.checkAccessToken(any(ReqPostAuthCheckAccessTokenDtoV1.class))).willReturn(response);

        mockMvc.perform(post("/v1/auth/check-access-token")
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

    @Test
    @DisplayName("이전 토큰 무효화 요청 시 현재 사용자 정보를 사용해 서비스를 호출한다")
    void invalidateBeforeToken_delegatesToService() throws Exception {
        UUID authUserId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        List<String> roleList = List.of("USER", "ADMIN");
        CustomUserDetails principal = CustomUserDetails.builder()
                .id(authUserId)
                .username("dummy_user")
                .email("dummy@example.com")
                .roleList(roleList)
                .build();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ReqPostAuthInvalidateBeforeTokenDtoV1 request = ReqPostAuthInvalidateBeforeTokenDtoV1.builder()
                .user(
                        ReqPostAuthInvalidateBeforeTokenDtoV1.UserDto.builder()
                                .id(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                                .build()
                )
                .build();

        willDoNothing().given(authServiceV1)
                .invalidateBeforeToken(any(UUID.class), anyList(), any(ReqPostAuthInvalidateBeforeTokenDtoV1.class));

        mockMvc.perform(post("/v1/auth/invalidate-before-token")
                        .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo("모든 기기에서 로그아웃 되었습니다.")))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "auth-invalidate-before-token",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Auth V1")
                                                .summary("이전 토큰 무효화")
                                                .description("사용자 기준으로 이전에 발급된 토큰을 무효화합니다.")
                                                .build()
                                )
                        )
                );

        ArgumentCaptor<List<String>> roleCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<ReqPostAuthInvalidateBeforeTokenDtoV1> requestCaptor = ArgumentCaptor.forClass(ReqPostAuthInvalidateBeforeTokenDtoV1.class);

        Mockito.verify(authServiceV1)
                .invalidateBeforeToken(Mockito.eq(authUserId), roleCaptor.capture(), requestCaptor.capture());

        assertEquals(roleList, roleCaptor.getValue());
        assertEquals(request.getUser().getId(), requestCaptor.getValue().getUser().getId());
    }
}
