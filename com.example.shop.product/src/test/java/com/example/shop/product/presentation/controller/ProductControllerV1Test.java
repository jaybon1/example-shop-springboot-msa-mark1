package com.example.shop.product.presentation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.example.shop.product.presentation.dto.request.ReqPostProductsDtoV1;
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

@WebMvcTest(value = ProductControllerV1.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.config.import=optional:classpath:/"
})
@AutoConfigureRestDocs
class ProductControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("상품 목록 조회 시 더미 데이터가 반환된다")
    void getProducts_returnsDummyList() throws Exception {
        mockMvc.perform(get("/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.products", hasSize(2)))
                .andExpect(jsonPath("$.data.products[0].name", equalTo("샘플 상품 A")))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "product-get-products",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Product V1")
                                                .summary("상품 목록 조회")
                                                .description("등록된 상품 목록을 조회합니다.")
                                                .build()
                                )
                        )
                );
    }

    @Test
    @DisplayName("상품 단건 조회 시 요청한 ID가 응답에 포함된다")
    void getProduct_returnsRequestedId() throws Exception {
        UUID productId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

        mockMvc.perform(get("/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.product.id", equalTo(productId.toString())))
                .andExpect(jsonPath("$.data.product.name", equalTo("단일 상품")))
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "product-get-product",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Product V1")
                                                .summary("상품 단건 조회")
                                                .description("지정한 상품 ID 로 단일 상품을 조회합니다.")
                                                .pathParameters(
                                                        ResourceDocumentation.parameterWithName("id")
                                                                .type(SimpleType.STRING)
                                                                .description("조회할 상품 ID")
                                                )
                                                .build()
                                )
                        )
                );
    }

    @Test
    @DisplayName("상품 등록 요청 시 더미 ID와 메시지를 반환한다")
    void createProduct_returnsDummyResponse() throws Exception {
        ReqPostProductsDtoV1 request = ReqPostProductsDtoV1.builder()
                .product(
                        ReqPostProductsDtoV1.ProductDto.builder()
                                .name("신규 상품")
                                .price(1000L)
                                .stock(3L)
                                .build()
                )
                .build();

        mockMvc.perform(post("/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo("상품 등록이 완료되었습니다.")))
                .andExpect(jsonPath("$.data.product.id").exists())
                .andDo(
                        MockMvcRestDocumentationWrapper.document(
                                "product-create-product",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                ResourceDocumentation.resource(
                                        ResourceSnippetParameters.builder()
                                                .tag("Product V1")
                                                .summary("상품 등록")
                                                .description("새로운 상품을 등록합니다.")
                                                .build()
                                )
                        )
                );
    }
}
