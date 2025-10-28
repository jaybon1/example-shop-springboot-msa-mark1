package com.example.shop.order.presentation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.example.shop.order.presentation.dto.request.ReqPostOrdersDtoV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = OrderControllerV1.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.config.import=optional:classpath:/"
})
@AutoConfigureRestDocs
class OrderControllerV1Test {

    private static final String DUMMY_BEARER_TOKEN = "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("주문 목록 조회 시 두 건의 더미 주문을 반환한다")
    void getOrders_returnsDummyOrders() throws Exception {
        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orders", hasSize(2)))
                .andExpect(jsonPath("$.data.orders[0].status", equalTo("CREATED")))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "order-get-orders",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Order V1")
                                                .summary("주문 목록 조회")
                                                .description("등록된 주문 목록을 조회합니다.")
                                                .build()
                                )
                        )
                );
    }

    @Test
    @DisplayName("주문 단건 조회 시 주문 ID 가 응답에 포함된다")
    void getOrder_returnsRequestedId() throws Exception {
        UUID orderId = UUID.fromString("aaaaaaaa-0000-0000-0000-aaaaaaaa0000");

        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/v1/orders/{id}", orderId)
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.order.id", equalTo(orderId.toString())))
                .andExpect(jsonPath("$.data.order.orderItemList", hasSize(2)))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "order-get-order",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Order V1")
                                                .summary("주문 단건 조회")
                                                .description("지정한 주문 ID로 단일 주문을 조회합니다.")
                                                .pathParameters(
                                                        ResourceDocumentation.parameterWithName("id")
                                                                .type(SimpleType.STRING)
                                                                .description("조회할 주문 ID")
                                                )
                                                .build()
                                )
                        )
                );
    }

    @Test
    @DisplayName("주문 생성 요청 시 더미 주문 정보와 메시지를 반환한다")
    void createOrder_returnsDummyOrder() throws Exception {
        ReqPostOrdersDtoV1 request = ReqPostOrdersDtoV1.builder()
                .order(
                        ReqPostOrdersDtoV1.OrderDto.builder()
                                .orderItemList(List.of(
                                        ReqPostOrdersDtoV1.OrderDto.OrderItemDto.builder()
                                                .productId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                                                .quantity(1L)
                                                .build()
                                ))
                                .build()
                )
                .build();

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/v1/orders")
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo("주문이 생성되었습니다.")))
                .andExpect(jsonPath("$.data.order.status", equalTo("CREATED")))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "order-create-order",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Order V1")
                                                .summary("주문 생성")
                                                .description("새로운 주문을 생성합니다.")
                                                .build()
                                )
                        )
                );
    }

    @Test
    @DisplayName("주문 취소 요청 시 성공 메시지를 반환한다")
    void cancelOrder_returnsSuccessMessage() throws Exception {
        UUID orderId = UUID.fromString("bbbbbbbb-0000-0000-0000-bbbbbbbb0000");

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/v1/orders/{id}/cancel", orderId)
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo(orderId + " 주문이 취소되었습니다.")))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "order-cancel-order",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Order V1")
                                                .summary("주문 취소")
                                                .description("지정한 주문 ID로 주문을 취소합니다.")
                                                .pathParameters(
                                                        ResourceDocumentation.parameterWithName("id")
                                                                .type(SimpleType.STRING)
                                                                .description("취소할 주문 ID")
                                                )
                                                .build()
                                )
                        )
                );
    }
}
