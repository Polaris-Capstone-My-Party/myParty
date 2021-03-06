package com.myParty.models;


import lombok.*;
import javax.persistence.*;

@ToString
@Entity
@Table(name = "item_bringer")
@AllArgsConstructor
@NoArgsConstructor
public class ItemBringer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private Long id;

    @Column(nullable = false)
    @Getter @Setter private Long quantity;

    @ManyToOne
    @JoinColumn(name = "guest_id")
    @Getter @Setter private Guest guest;

    @ManyToOne
    @JoinColumn(name = "partyMember_id")
    @Getter @Setter private PartyMember partyMember;

    @ManyToOne
    @JoinColumn(name = "party_item_id")
    @Getter @Setter private PartyItem partyItem;

}
