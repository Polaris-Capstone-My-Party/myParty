package com.myParty.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "members")
@NoArgsConstructor
@AllArgsConstructor

public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private Long id;

    @Column(nullable = false)
    @Getter @Setter private String username;

    @Column(nullable = false)
    @Getter @Setter private String email;

    @Column(nullable = false)
    @Getter @Setter private String password;

    @Column(nullable = false)
    @Getter @Setter private Long phone;

    @Column(nullable = false)
    @Getter @Setter private String first_name;

    @Column(nullable = false)
    @Getter @Setter private String last_name;

    public Member (Member copy) {
        id = copy.id; // This line is SUPER important! Many things won't work if it's absent
        email = copy.email;
        username = copy.username;
        password = copy.password;
    }

}
