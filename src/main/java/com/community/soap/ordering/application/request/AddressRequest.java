package com.community.soap.ordering.application.request;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String receiverName,
        @NotBlank String phone,
        @NotBlank String zipcode,
        @NotBlank String address1,
        String address2
) {

}
