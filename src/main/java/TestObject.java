import java.util.List;

public class TestObject {
    private String id;
    private String name;
    private List<ItemDetail> listItemDetails;
   
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ItemDetail> getListItemDetails() {
        return listItemDetails;
    }

    public void setListItemDetails(List<ItemDetail> listItemDetails) {
        this.listItemDetails = listItemDetails;
    }

    @Override
    public String toString() {
        return "{id: " + this.id + ", name: " + this.name + "}";
    }
}
