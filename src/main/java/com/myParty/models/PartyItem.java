package com.myParty.models;

import lombok.*;
import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "party_items")
@AllArgsConstructor
@NoArgsConstructor
public class PartyItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private Long id;

    @Column(nullable = false)
    @Getter @Setter private Long quantityRequired;

    @ManyToOne
    @JoinColumn(name = "party_id")
    @Getter @Setter private Party party;

    @ManyToOne
    @JoinColumn(name = "item_id")
    @Getter @Setter private Item item;

    @OneToMany(mappedBy="partyItem",
            cascade= {CascadeType.REMOVE})
    @Getter @Setter private List<ItemBringer> itemBringers;

}
