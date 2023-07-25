package local;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DetailedOrder extends RequestOrder {

    private String id;
    private String createdBy;
    private Integer quantity;
    private Long timestamp;

    @JsonCreator
    public DetailedOrder(
            @JsonProperty("id") String id,
            @JsonProperty("bookId") Integer bookId,
            @JsonProperty("customerName") String customerName,
            @JsonProperty("createdBy") String createdBy,
            @JsonProperty("quantity") Integer quantity,
            @JsonProperty("timestamp") Long timestamp) {
        super(bookId, customerName);
        this.id = id;
        this.createdBy = createdBy;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Long getTimestamp() {
        return timestamp;
    }

}
