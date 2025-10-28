package com.example.shop.product.presentation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.example.shop.product.application.service.ProductServiceV1;
import com.example.shop.product.infrastructure.security.jwt.JwtProperties;
import com.example.shop.product.presentation.dto.request.ReqPostInternalProductsReleaseStockDtoV1;
import com.example.shop.product.presentation.dto.request.ReqPostInternalProductsReturnStockDtoV1;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
    @DisplayName("내부 API - 재고 차감 요청이 성공하면 메시지를 반환한다")
    void releaseProductStock_returnsMessage() throws Exception {
        ReqPostInternalProductsReleaseStockDtoV1 request = ReqPostInternalProductsReleaseStockDtoV1.builder()
                .order(
                        ReqPostInternalProductsReleaseStockDtoV1.OrderDto.builder()
                                .orderId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                                .build()
                )
                .productStocks(List.of(
                        ReqPostInternalProductsReleaseStockDtoV1.ProductStockDto.builder()
                                .productId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"))
                                .quantity(3L)
                                .build()
                ))
                .build();

        willDoNothing().given(productServiceV1).postInternalProductsReleaseStock(any(ReqPostInternalProductsReleaseStockDtoV1.class));

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/internal/v1/products/release-stock")
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo("상품 재고 차감이 완료되었습니다.")))
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
                                                .build()
                                )
                        )
                );
    }

    @Test
    @DisplayName("내부 API - 재고 복원 요청이 성공하면 메시지를 반환한다")
    void returnProductStock_returnsMessage() throws Exception {
        ReqPostInternalProductsReturnStockDtoV1 request = ReqPostInternalProductsReturnStockDtoV1.builder()
                .order(
                        ReqPostInternalProductsReturnStockDtoV1.OrderDto.builder()
                                .orderId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"))
                                .build()
                )
                .build();

        willDoNothing().given(productServiceV1).postInternalProductsReturnStock(any(ReqPostInternalProductsReturnStockDtoV1.class));

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/internal/v1/products/return-stock")
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo("상품 재고 복원이 완료되었습니다.")))
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
                                                .build()
                                )
                        )
                );
    }
}
