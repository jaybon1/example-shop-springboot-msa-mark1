package com.example.shop.payment.presentation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.shop.payment.application.service.PaymentServiceV1;
import com.example.shop.payment.infrastructure.security.auth.CustomUserDetails;
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
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = InternalPaymentControllerV1.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.config.import=optional:classpath:/"
})
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
class InternalPaymentControllerV1Test {

    private static final String TEST_ACCESS_JWT =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";
    private static final String DUMMY_BEARER_TOKEN = "Bearer " + TEST_ACCESS_JWT;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentServiceV1 paymentServiceV1;

    private static final CustomUserDetails TEST_USER_DETAILS = CustomUserDetails.builder()
            .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
            .username("internal-tester")
            .nickname("Internal Tester")
            .email("internal@test.com")
            .accessJwt(TEST_ACCESS_JWT)
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

    @Test
    @DisplayName("내부 API - 결제 취소 요청이 성공하면 메시지를 반환한다")
    void postInternalPaymentsCancel_returnsMessage() throws Exception {
        UUID paymentId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        willDoNothing().given(paymentServiceV1).postInternalPaymentsCancel(eq(paymentId));

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/internal/v1/payments/{id}/cancel", paymentId)
                                .with(user(TEST_USER_DETAILS))
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo(paymentId + " 결제가 취소되었습니다.")))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "payment-internal-cancel-payment",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Payment Internal V1")
                                                .summary("결제 취소")
                                                .description("내부 결제 취소 요청을 전달해 결제를 CANCELLED 상태로 전환합니다.")
                                                .build()
                                )
                        )
                );
    }
}
