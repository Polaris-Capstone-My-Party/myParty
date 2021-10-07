package com.myParty.models;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "party_items")
@AllArgsConstructor
@NoArgsConstructor
public class Party_Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private Long id;

    @Column(nullable = false)
    @Getter @Setter private String name;

    @Column(nullable = false)
    @Getter @Setter private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "party_id")
    @Getter @Setter private Party party;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    @Getter @Setter private Guest guest;

    @ManyToOne
    @JoinColumn(name = "member_id")
    @Getter @Setter private Member member;

    @ManyToOne
    @JoinColumn(name = "item_id")
    @Getter @Setter private Item item;




}
