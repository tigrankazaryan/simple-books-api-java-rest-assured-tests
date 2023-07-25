package local;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Client {

    private String clientName;
    private String clientEmail;

    @JsonCreator
    public Client(
            @JsonProperty("clientName") String clientName,
            @JsonProperty("clientEmail") String clientEmail) {
        this.clientName = clientName;
        this.clientEmail = clientEmail;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

}
