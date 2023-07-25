package local;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreatedOrder {

    private Boolean created;
    private String orderId;

    @JsonCreator
    public CreatedOrder(
            @JsonProperty("created") Boolean created,
            @JsonProperty("orderId") String orderId) {
        this.created = created;
        this.orderId = orderId;
    }

    public Boolean getCreated() {
        return created;
    }

    public String getOrderId() {
        return orderId;
    }

}
