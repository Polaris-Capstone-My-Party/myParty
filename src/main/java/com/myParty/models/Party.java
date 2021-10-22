package com.myParty.models;

import lombok.*;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@ToString
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

    @Column(nullable = false, name = "start_time")
    @Getter @Setter private Timestamp startTime;

    @Column(nullable = false, name = "end_time")
    @Getter @Setter private Timestamp endTime;

    @Column(nullable = false, name="url_key")
    @Getter @Setter private String urlKey;

    @ManyToOne
    @JoinColumn (name = "member_id")
    @Getter @Setter private Member owner;

    @OneToOne
    @JoinColumn(name = "location_id")
    @Getter @Setter private Location location;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name="parties_tags",
            joinColumns={@JoinColumn(name="party_id")},
            inverseJoinColumns={@JoinColumn(name="tag_id")}
    ) @Getter @Setter private List<Tag> tags;

    @OneToMany(mappedBy="party",
            cascade= {CascadeType.REMOVE})
    @Getter @Setter private List<Guest> guests;

//    public Party(String title, String description, String member) {
//        this.title = title;
//        this.description = description;
//        this.owner = member;
    }



