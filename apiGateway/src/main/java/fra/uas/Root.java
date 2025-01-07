package fra.uas;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Root {

    @JsonProperty("data")
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
