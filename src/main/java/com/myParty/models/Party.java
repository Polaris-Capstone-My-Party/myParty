package com.myParty.models;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//@ToString
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
//    @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm")
    @Getter @Setter private Timestamp startTime;

    @Column(nullable = false, name = "end_time")
//    @DateTimeFormat(pattern = "MM-dd-yyyy HH:mm")
    @Getter @Setter private Timestamp endTime;

    @Column(nullable = false, name="url_key")
    @Getter @Setter private String urlKey;

    @ManyToOne
    @JoinColumn (name = "member_id")
    @Getter @Setter private Member owner;

    @OneToOne
    @JoinColumn(name = "location_id")
    @Getter @Setter private Location location;

    @OneToMany(mappedBy="party",
            cascade= {CascadeType.REMOVE})
    @Getter @Setter private List<Guest> guests;

    @OneToMany(mappedBy="party",
            cascade= {CascadeType.REMOVE})
    @Getter @Setter private List<PartyItem> partyItems;

    @OneToMany(mappedBy="party",
            cascade= {CascadeType.REMOVE})
    @Getter @Setter private List<PartyMember> partyMembers;

    public Timestamp makeTimestampFromString(String datetime){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(datetime.replace("T", " "));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());

        return timestamp;
    }

    public String convertTimestamp(Timestamp timestamp){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM-dd-yyyy hh:mm aa");

        Date date = new Date(timestamp.getTime());
        String formattedDate = dateFormat.format(date);

        formattedDate = formattedDate.replace("-", " ");

        return formattedDate;
    }

}



