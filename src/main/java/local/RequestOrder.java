package local;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestOrder {

    private Integer bookId;
    private String customerName;

    @JsonCreator
    public RequestOrder(
            @JsonProperty("bookId") Integer bookId,
            @JsonProperty("customerName") String customerName) {
        this.bookId = bookId;
        this.customerName = customerName;
    }

    public Integer getBookId() {
        return bookId;
    }

    public String getCustomerName() {
        return customerName;
    }

}
