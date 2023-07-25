package local;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BookFromList {

    private Integer id;
    private String name;
    private String type;
    private Boolean available;

    @JsonCreator
    public BookFromList(
            @JsonProperty("id") Integer id,
            @JsonProperty("name") String name,
            @JsonProperty("type") String type,
            @JsonProperty("available") Boolean available) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.available = available;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Boolean getAvailable() {
        return available;
    }

}
