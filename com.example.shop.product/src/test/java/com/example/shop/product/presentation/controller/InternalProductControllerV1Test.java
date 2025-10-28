package com.example.shop.product.presentation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.example.shop.product.application.service.ProductServiceV1;
import com.example.shop.product.infrastructure.security.jwt.JwtProperties;
import com.example.shop.product.presentation.dto.request.ReqPostInternalProductReleaseStockDtoV1;
import com.example.shop.product.presentation.dto.request.ReqPostInternalProductReturnStockDtoV1;
import com.example.shop.product.presentation.dto.response.ResPostInternalProductReleaseStockDtoV1;
import com.example.shop.product.presentation.dto.response.ResPostInternalProductReturnStockDtoV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(value = InternalProductControllerV1.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.config.import=optional:classpath:/",
        "shop.security.jwt.access-header-name=Authorization",
        "shop.security.jwt.header-prefix=Bearer ",
        "shop.security.jwt.access-subject=access-token"
})
@AutoConfigureRestDocs
@AutoConfigureMockMvc(addFilters = false)
class InternalProductControllerV1Test {

    private static final String DUMMY_BEARER_TOKEN = "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductServiceV1 productServiceV1;

    @TestConfiguration
    static class JwtTestConfig {
        @Bean
        JwtProperties jwtProperties() {
            return new JwtProperties("Authorization", "Bearer ", "access-token");
        }
    }

    @Test
    @DisplayName("내부 API - 재고 차감 요청이 성공하면 PRODUCT_STOCK_RELEASED 응답을 반환한다")
    void releaseProductStock_returnsReleasedCode() throws Exception {
        ReqPostInternalProductReleaseStockDtoV1 request = ReqPostInternalProductReleaseStockDtoV1.builder()
                .orderId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .quantity(3L)
                .build();

        UUID productId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        ResPostInternalProductReleaseStockDtoV1 serviceResponse = ResPostInternalProductReleaseStockDtoV1.builder()
                .productId(productId.toString())
                .orderId(request.getOrderId().toString())
                .releasedQuantity(request.getQuantity())
                .currentStock(Math.max(0, 100 - request.getQuantity()))
                .build();
        given(productServiceV1.postInternalProductReleaseStock(eq(productId), any(ReqPostInternalProductReleaseStockDtoV1.class)))
                .willReturn(serviceResponse);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/internal/v1/products/{productId}/release-stock", productId)
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("PRODUCT_STOCK_RELEASED")))
                .andExpect(jsonPath("$.data.productId", equalTo(productId.toString())))
                .andExpect(jsonPath("$.data.orderId", equalTo(request.getOrderId().toString())))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "product-internal-release-stock",
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
        ReqPostInternalProductReturnStockDtoV1 request = ReqPostInternalProductReturnStockDtoV1.builder()
                .orderId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"))
                .quantity(2L)
                .build();

        UUID productId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

        ResPostInternalProductReturnStockDtoV1 serviceResponse = ResPostInternalProductReturnStockDtoV1.builder()
                .productId(productId.toString())
                .orderId(request.getOrderId().toString())
                .returnedQuantity(request.getQuantity())
                .currentStock(100 + request.getQuantity())
                .build();
        given(productServiceV1.postInternalProductReturnStock(eq(productId), any(ReqPostInternalProductReturnStockDtoV1.class)))
                .willReturn(serviceResponse);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/internal/v1/products/{productId}/return-stock", productId)
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo("PRODUCT_STOCK_RETURNED")))
                .andExpect(jsonPath("$.data.productId", equalTo(productId.toString())))
                .andExpect(jsonPath("$.data.orderId", equalTo(request.getOrderId().toString())))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "product-internal-return-stock",
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
