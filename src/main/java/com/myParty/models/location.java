package com.myParty.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

public class location {

    @Entity
    @Table(name="locations")
    @NoArgsConstructor
    @AllArgsConstructor

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private long id;

    @Column(nullable = false)
    @Getter @Setter private String address_one;

    @Column(nullable = false)
    @Getter @Setter private String address_two;

    @Column(nullable = false)
    @Getter @Setter private String city;

    @Column(nullable = false)
    @Getter @Setter private String state;

    @Column(nullable = false)
    @Getter @Setter private String zipcode;

}

