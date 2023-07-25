package local;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SingleBook extends BookFromList {

    private String author;
    private String isbn;
    private Double price;
    private Integer currentStock;

    @JsonCreator
    public SingleBook(
            @JsonProperty("id") Integer id,
            @JsonProperty("name") String name,
            @JsonProperty("author") String author,
            @JsonProperty("isbn") String isbn,
            @JsonProperty("type") String type,
            @JsonProperty("price") Double price,
            @JsonProperty("current-stock") Integer currentStock,
            @JsonProperty("available") Boolean available) {
        super(id, name, type, available);
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.currentStock = currentStock;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

}
