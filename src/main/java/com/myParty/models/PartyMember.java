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
public class PartyMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private Long id;

    @Column(nullable = false)
    @Getter @Setter private RsvpStatuses rsvpStatus;

    @ManyToOne
    @JoinColumn(name = "party_id")
    @Getter @Setter private Party party;

    @ManyToOne
    @JoinColumn(name = "member_id")
    @Getter @Setter private Member member;

    @Column(nullable = false, name = "partyMember_key")
    @Getter @Setter private String partyMemberKey;

    @Column(name = "addtl_guests")
    @Getter @Setter private String additionalGuests;
}
