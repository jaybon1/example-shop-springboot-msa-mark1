package com.example.shop.gateway.presentation.dto;

import com.example.shop.gateway.infrastructure.constants.Constants;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiDto<T> {

    @Builder.Default
    private String code = Constants.ApiCode.SUCCESS.toString();

    @Builder.Default
    private String message = "success";

    private T data;

}
