package com.example.shop.payment.presentation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.example.shop.payment.presentation.dto.request.ReqPostPaymentsDtoV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = PaymentControllerV1.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.config.import=optional:classpath:/"
})
@AutoConfigureRestDocs
class PaymentControllerV1Test {

    private static final String DUMMY_BEARER_TOKEN = "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("결제 단건 조회 시 요청한 ID가 포함된 더미 데이터를 반환한다")
    void getPayment_returnsDummyData() throws Exception {
        UUID paymentId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/v1/payments/{id}", paymentId)
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.payment.id", equalTo(paymentId.toString())))
                .andExpect(jsonPath("$.data.payment.status", equalTo("COMPLETED")))
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
    @DisplayName("결제 생성 요청 시 더미 결제 정보와 메시지를 반환한다")
    void createPayment_returnsDummyResponse() throws Exception {
        ReqPostPaymentsDtoV1 request = ReqPostPaymentsDtoV1.builder()
                .orderId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                .method("CARD")
                .amount(50_000L)
                .build();

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/v1/payments")
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
