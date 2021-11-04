package com.myParty.models;

import lombok.*;

import javax.persistence.*;
import java.util.List;

//@ToString
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

    @Column(nullable = false, name = "first_name")
    @Getter @Setter private String firstName;

    @Column(nullable = false, name = "last_name")
    @Getter @Setter private String lastName;

    @Column
    @Getter @Setter private String resetToken;

    @Getter @Setter
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private List<Party> party;

    @OneToMany(mappedBy="member",
            cascade= {CascadeType.REMOVE})
    @Getter @Setter private List<PartyMember> partyMembers;

    public Member(String username, String email, String password, List<Party> party) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.party = party;
    }

    public List<Party> getParty() {
        return party;
    }

    public void setParty(List<Party> party) {
        this.party = party;
    }
    public Member (Member copy) {
        id = copy.id; // This line is SUPER important! Many things won't work if it's absent
        email = copy.email;
        username = copy.username;
        password = copy.password;
    }

}
