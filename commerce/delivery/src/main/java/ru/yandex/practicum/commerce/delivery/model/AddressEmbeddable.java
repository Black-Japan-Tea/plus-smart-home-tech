package ru.yandex.practicum.commerce.delivery.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class AddressEmbeddable {

    @Column(name = "from_country")
    private String country;

    @Column(name = "from_city")
    private String city;

    @Column(name = "from_street")
    private String street;

    @Column(name = "from_house")
    private String house;

    @Column(name = "from_flat")
    private String flat;
}

