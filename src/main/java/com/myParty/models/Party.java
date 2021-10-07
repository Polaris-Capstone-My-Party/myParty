package com.myParty.models;

import javax.persistence.*;
import java.lang.reflect.Member;
import java.sql.Timestamp;

@Entity
@Table(name = "parties")
public class Party {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Timestamp start_time;

    @Column(nullable = false)
    private Timestamp end_time;

    @Column(nullable = false)
    private String url_key;

    @ManyToOne
    @JoinColumn (name = "member_id")
    private Member owner;

    @OneToOne
    @JoinColumn(name = "location_id")
    private Location location;

}
