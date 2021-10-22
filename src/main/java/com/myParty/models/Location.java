package com.myParty.models;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="locations")
@NoArgsConstructor
@AllArgsConstructor
@ToString


public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private long id;

    @Column(nullable = false, name="address_one")
    @Getter @Setter private String addressOne;

    @Column(name = "address_two")
    @Getter @Setter private String addressTwo;

    @Column(nullable = false)
    @Getter @Setter private String city;

    @Column(nullable = false)
    @Getter @Setter private String state;

    @Column(nullable = false)
    @Getter @Setter private String zipcode;

}

