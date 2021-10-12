package com.myParty.models;

import lombok.*;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "parties")
@AllArgsConstructor
@NoArgsConstructor
public class Party {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private Long id;

    @Column(nullable = false)
    @Getter @Setter private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Getter @Setter private String description;

    //TODO: chance back to nullable false
    @Column
    @Getter @Setter private Timestamp start_time;

    //TODO: chance back to nullable false
    @Column
    @Getter @Setter private Timestamp end_time;

    @Column(nullable = false)
    @Getter @Setter private String url_key;

    @ManyToOne
    @JoinColumn (name = "member_id")
    @Getter @Setter private Member owner;

    @OneToOne
    @JoinColumn(name = "location_id")
    @Getter @Setter private Location location;

    //added many to many
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name="parties_tags",
            joinColumns={@JoinColumn(name="party_id")},
            inverseJoinColumns={@JoinColumn(name="tag_id")}
    )
    private List<Tag> tags;

}
