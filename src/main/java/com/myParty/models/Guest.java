package com.myParty.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name="guests")
@NoArgsConstructor
@AllArgsConstructor

public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private long id;

    @Column(nullable = false)
    @Getter @Setter private String email;

    @Column(nullable = false)
    @Getter @Setter private String first_name;

    @Column(nullable = false)
    @Getter @Setter private String last_name;

    @Column(nullable = false)
    @Getter @Setter private String rsvp_status;

    @ManyToOne
    @JoinColumn (name = "party_id")
    @Getter @Setter private Party party;

}
