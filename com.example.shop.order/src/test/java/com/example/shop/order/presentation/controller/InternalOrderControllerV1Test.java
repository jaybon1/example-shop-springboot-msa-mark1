package com.example.shop.order.presentation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.shop.order.application.service.OrderServiceV1;
import com.example.shop.order.domain.vo.OrderPayment;
import com.example.shop.order.infrastructure.security.auth.CustomUserDetails;
import com.example.shop.order.infrastructure.security.jwt.JwtProperties;
import com.example.shop.order.presentation.dto.request.ReqPostInternalOrderCompleteDtoV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = InternalOrderControllerV1.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.config.import=optional:classpath:/"
})
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
class InternalOrderControllerV1Test {

    private static final String DUMMY_BEARER_TOKEN = "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderServiceV1 orderServiceV1;

    private static final CustomUserDetails TEST_USER_DETAILS = CustomUserDetails.builder()
            .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
            .username("internal-tester")
            .nickname("Internal Tester")
            .email("internal@test.com")
            .roleList(List.of("SYSTEM"))
            .build();

    @BeforeEach
    void setUpSecurityContext() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                TEST_USER_DETAILS,
                null,
                TEST_USER_DETAILS.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @TestConfiguration
    static class JwtTestConfig {
        @Bean
        JwtProperties jwtProperties() {
            return new JwtProperties("Authorization", "Bearer ", "access-token");
        }
    }

    @Test
    @DisplayName("내부 API - 주문 결제 완료 요청이 성공하면 메시지를 반환한다")
    void postInternalOrdersComplete_returnsMessage() throws Exception {
        UUID orderId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        ReqPostInternalOrderCompleteDtoV1 request = ReqPostInternalOrderCompleteDtoV1.builder()
                .payment(
                        ReqPostInternalOrderCompleteDtoV1.PaymentDto.builder()
                                .paymentId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                                .amount(150_000L)
                                .method(OrderPayment.Method.CARD)
                                .build()
                )
                .build();

        willDoNothing().given(orderServiceV1).postInternalOrdersComplete(eq(orderId), any(ReqPostInternalOrderCompleteDtoV1.class));

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/internal/v1/orders/{id}/complete", orderId)
                                .with(user(TEST_USER_DETAILS))
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo(orderId + " 주문이 결제 완료되었습니다.")))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "order-internal-complete-order",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Order Internal V1")
                                                .summary("주문 결제 완료 처리")
                                                .description("내부 결제 완료 요청을 전달해 주문을 PAID 상태로 전환합니다.")
                                                .build()
                                )
                        )
                );
    }
}
