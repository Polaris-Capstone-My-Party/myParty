package com.myParty.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "party_members")
@NoArgsConstructor
@AllArgsConstructor
public class Party_Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private Long id;

    @Column(nullable = false)
    @Getter @Setter private RsvpStatuses rsvpStatuses ;

    @ManyToOne
    @JoinColumn(name = "party_id")
    @Getter @Setter private Party party;

    @ManyToOne
    @JoinColumn(name = "member_id")
    @Getter @Setter private Member member;

}
