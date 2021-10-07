package com.myParty.models;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="tags")
@NoArgsConstructor
public class Tags {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private long id;

    @Column(nullable = false)
    @Getter @Setter private String name;

    @ManyToMany(mappedBy = "tags")
    @Getter @Setter private List<Party> parties;
}