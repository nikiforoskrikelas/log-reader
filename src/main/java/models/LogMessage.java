package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties
public class LogMessage {
    private String id;
    private Boolean started;
    private Long timestamp;

    private String type;
    private String host;

    @JsonProperty("state")
    public void setStarted(String started) {
        //Assuming only STARTED and FINISHED states, not sure though, so Boolean allows for unknown state
        switch (started) {
            case "STARTED":
                this.started = true;
                break;
            case "FINISHED":
                this.started = false;
                break;
            default:
                this.started = null;
        }

    }
}
