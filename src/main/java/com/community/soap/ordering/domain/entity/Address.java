package com.community.soap.ordering.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Address {

    private String receiverName;
    private String phone;
    private String zipcode;
    private String address1;
    private String address2;

    private Address(String receiverName, String phone, String zipcode, String address1,
            String address2) {
        this.receiverName = receiverName;
        this.phone = phone;
        this.zipcode = zipcode;
        this.address1 = address1;
        this.address2 = address2;
    }

    public static Address of(
            String receiverName,
            String phone,
            String zipcode,
            String address1,
            String address2) {
        return new Address(
                receiverName,
                phone,
                zipcode,
                address1,
                address2);
    }
}

