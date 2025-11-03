package com.example.shop.payment.presentation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.example.shop.payment.application.service.PaymentServiceV1;
import com.example.shop.payment.domain.model.Payment;
import com.example.shop.payment.infrastructure.security.auth.CustomUserDetails;
import com.example.shop.payment.infrastructure.security.jwt.JwtProperties;
import com.example.shop.payment.presentation.dto.request.ReqPostPaymentsDtoV1;
import com.example.shop.payment.presentation.dto.response.ResGetPaymentDtoV1;
import com.example.shop.payment.presentation.dto.response.ResPostPaymentsDtoV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
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

@WebMvcTest(value = PaymentControllerV1.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.config.import=optional:classpath:/"
})
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerV1Test {

    private static final String TEST_ACCESS_JWT =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";
    private static final String DUMMY_BEARER_TOKEN = "Bearer " + TEST_ACCESS_JWT;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentServiceV1 paymentServiceV1;

    private static final CustomUserDetails TEST_USER_DETAILS = CustomUserDetails.builder()
            .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
            .username("test-user")
            .nickname("tester")
            .email("tester@example.com")
            .accessJwt(TEST_ACCESS_JWT)
            .roleList(List.of("USER"))
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
    @DisplayName("결제 단건 조회 시 요청한 ID가 포함된 응답을 반환한다")
    void getPayment_returnsPayment() throws Exception {
        UUID paymentId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

        ResGetPaymentDtoV1 response = ResGetPaymentDtoV1.builder()
                .payment(
                        ResGetPaymentDtoV1.PaymentDto.builder()
                                .id(paymentId.toString())
                                .status("COMPLETED")
                                .method("CARD")
                                .amount(120_000L)
                                .approvedAt(Instant.now())
                                .transactionKey("tx-1234")
                                .orderId(UUID.randomUUID().toString())
                                .build()
                )
                .build();
        given(paymentServiceV1.getPayment(any(), eq(paymentId))).willReturn(response);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/v1/payments/{id}", paymentId)
                                .with(user(TEST_USER_DETAILS))
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.payment.id", equalTo(paymentId.toString())))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "payment-get-payment",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Payment V1")
                                                .summary("결제 단건 조회")
                                                .description("지정한 결제 ID로 결제 내역을 조회합니다.")
                                                .pathParameters(
                                                        ResourceDocumentation.parameterWithName("id")
                                                                .type(SimpleType.STRING)
                                                                .description("조회할 결제 ID")
                                                )
                                                .build()
                                )
                        )
                );
    }

    @Test
    @DisplayName("결제 생성 요청 시 결제 정보와 메시지를 반환한다")
    void postPayments_returnsCreatedPayment() throws Exception {
        ReqPostPaymentsDtoV1 request = ReqPostPaymentsDtoV1.builder()
                .payment(
                        ReqPostPaymentsDtoV1.PaymentDto.builder()
                                .orderId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                                .method(Payment.Method.CARD)
                                .amount(50_000L)
                                .build()
                )
                .build();

        ResPostPaymentsDtoV1 response = ResPostPaymentsDtoV1.builder()
                .payment(
                        ResPostPaymentsDtoV1.PaymentDto.builder()
                                .id(UUID.randomUUID().toString())
                                .status("COMPLETED")
                                .method("CARD")
                                .amount(50_000L)
                                .approvedAt(Instant.now())
                                .transactionKey("tx-9999")
                                .orderId(request.getPayment().getOrderId().toString())
                                .build()
                )
                .build();
        given(paymentServiceV1.postPayments(any(), eq(TEST_ACCESS_JWT), any(ReqPostPaymentsDtoV1.class))).willReturn(response);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/v1/payments")
                                .with(user(TEST_USER_DETAILS))
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo("결제가 완료되었습니다.")))
                .andExpect(jsonPath("$.data.payment.method", equalTo("CARD")))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "payment-create-payment",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Payment V1")
                                                .summary("결제 생성")
                                                .description("주문에 대해 결제를 생성합니다.")
                                                .build()
                                )
                        )
                );
    }
}
