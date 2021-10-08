package com.myParty.models;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="tags")
@NoArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter private long id;

    @Column(nullable = false)
    @Getter @Setter private String name;

    @ManyToMany(mappedBy = "tags")
    @Getter @Setter private List<Party> parties;
}