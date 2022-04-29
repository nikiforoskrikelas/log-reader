package com.creditsuisse.logreader.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sonatype.inject.Nullable;

import javax.persistence.*;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@JsonIgnoreProperties
public class LogMessage {
//Perhaps different Objects could be used for parsing and for storing in the DB as well
    @Id
    @JsonProperty("id")
    @Column(name = "id")
    private String id;

    //@JsonInclude() & @Transient allows a field to be parsed from json, but not stored in the db
    @JsonInclude()
    @Transient
    private Boolean isStarted;

    @JsonInclude()
    @Transient
    private long timestamp;

    @Column(name = "timestamp")
    @JsonIgnore
    private long duration;

    @Column(name = "alert")
    @JsonIgnore
    private boolean alert;

    @Nullable
    @Column(name = "type")
    private String type;

    @Nullable
    @Column(name = "host")
    private String host;

    @JsonProperty("state")
    public void setIsStarted(String isStarted) {
        //Assuming only STARTED and FINISHED states, not sure though, so Boolean allows for unknown state
        switch (isStarted) {
            case "STARTED":
                this.isStarted = true;
                break;
            case "FINISHED":
                this.isStarted = false;
                break;
            default:
                this.isStarted = null;
        }
    }

}
