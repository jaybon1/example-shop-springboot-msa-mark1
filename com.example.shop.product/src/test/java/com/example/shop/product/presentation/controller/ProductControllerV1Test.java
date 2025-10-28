package com.example.shop.product.presentation.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceDocumentation;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import com.example.shop.product.application.service.ProductServiceV1;
import com.example.shop.product.infrastructure.security.jwt.JwtProperties;
import com.example.shop.product.presentation.dto.request.ReqPostProductsDtoV1;
import com.example.shop.product.presentation.dto.response.ResGetProductDtoV1;
import com.example.shop.product.presentation.dto.response.ResGetProductsDtoV1;
import com.example.shop.product.presentation.dto.response.ResPostProductsDtoV1;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(value = ProductControllerV1.class, properties = {
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
class ProductControllerV1Test {

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
    @DisplayName("상품 목록 조회 시 더미 데이터가 반환된다")
    void getProducts_returnsDummyList() throws Exception {
        ResGetProductsDtoV1.ProductPageDto.ProductDto productA = ResGetProductsDtoV1.ProductPageDto.ProductDto.builder()
                .id(UUID.randomUUID().toString())
                .name("샘플 상품 A")
                .price(1000L)
                .stock(5L)
                .build();
        ResGetProductsDtoV1.ProductPageDto.ProductDto productB = ResGetProductsDtoV1.ProductPageDto.ProductDto.builder()
                .id(UUID.randomUUID().toString())
                .name("샘플 상품 B")
                .price(2000L)
                .stock(8L)
                .build();
        ResGetProductsDtoV1 serviceResponse = ResGetProductsDtoV1.builder()
                .productPage(new ResGetProductsDtoV1.ProductPageDto(productA, productB))
                .build();
        given(productServiceV1.getProducts(any(Pageable.class), any())).willReturn(serviceResponse);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productPage.content", hasSize(2)))
                .andExpect(jsonPath("$.data.productPage.content[0].name", equalTo("샘플 상품 A")))
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

        ResGetProductDtoV1 serviceResponse = ResGetProductDtoV1.builder()
                .product(
                        ResGetProductDtoV1.ProductDto.builder()
                                .id(productId.toString())
                                .name("단일 상품")
                                .price(15_000L)
                                .stock(7L)
                                .build()
                )
                .build();
        given(productServiceV1.getProduct(productId)).willReturn(serviceResponse);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/v1/products/{id}", productId)
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                )
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

        ResPostProductsDtoV1 serviceResponse = ResPostProductsDtoV1.builder()
                .product(
                        ResPostProductsDtoV1.ProductDto.builder()
                                .id(UUID.randomUUID().toString())
                                .build()
                )
                .build();
        given(productServiceV1.postProducts(any(ReqPostProductsDtoV1.class))).willReturn(serviceResponse);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/v1/products")
                                .header(HttpHeaders.AUTHORIZATION, DUMMY_BEARER_TOKEN)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
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
