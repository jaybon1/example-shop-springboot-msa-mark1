package com.example.shop.product.presentation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.example.shop.product.presentation.dto.request.ReqPostProductStockAdjustDtoV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ProductInternalControllerV1.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.config.import=optional:classpath:/"
})
@AutoConfigureRestDocs
class ProductInternalControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("내부 API - 재고 차감 요청이 성공하면 PRODUCT_STOCK_RELEASED 응답을 반환한다")
    void releaseProductStock_returnsReleasedCode() throws Exception {
        ReqPostProductStockAdjustDtoV1 request = ReqPostProductStockAdjustDtoV1.builder()
                .orderId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .quantity(3L)
                .build();

        UUID productId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        mockMvc.perform(post("/internal/v1/products/{productId}/stock-release", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("PRODUCT_STOCK_RELEASED")))
                .andExpect(jsonPath("$.data.productId", equalTo(productId.toString())))
                .andExpect(jsonPath("$.data.orderId", equalTo(request.getOrderId().toString())))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "product-internal-stock-release",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Product Internal V1")
                                                .summary("상품 재고 차감")
                                                .description("주문 생성 시 내부에서 상품 재고를 차감합니다.")
                                                .pathParameters(
                                                        ResourceDocumentation.parameterWithName("productId")
                                                                .type(SimpleType.STRING)
                                                                .description("재고를 차감할 상품 ID")
                                                )
                                                .build()
                                )
                        )
                );
    }

    @Test
    @DisplayName("내부 API - 재고 복원 요청이 성공하면 PRODUCT_STOCK_RETURNED 응답을 반환한다")
    void returnProductStock_returnsReturnedCode() throws Exception {
        ReqPostProductStockAdjustDtoV1 request = ReqPostProductStockAdjustDtoV1.builder()
                .orderId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"))
                .quantity(2L)
                .build();

        UUID productId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

        mockMvc.perform(post("/internal/v1/products/{productId}/stock-return", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("PRODUCT_STOCK_RETURNED")))
                .andExpect(jsonPath("$.data.productId", equalTo(productId.toString())))
                .andExpect(jsonPath("$.data.orderId", equalTo(request.getOrderId().toString())))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "product-internal-stock-return",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Product Internal V1")
                                                .summary("상품 재고 복원")
                                                .description("주문 취소 등으로 상품 재고를 복원합니다.")
                                                .pathParameters(
                                                        ResourceDocumentation.parameterWithName("productId")
                                                                .type(SimpleType.STRING)
                                                                .description("재고를 복원할 상품 ID")
                                                )
                                                .build()
                                )
                        )
                );
    }
}
