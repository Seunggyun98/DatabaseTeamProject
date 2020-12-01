package parser;

public class Item {
    String name;
    int price;
    String brand="";
    String event;

    public Item(String name, int price) {
        this.name=name;
        this.price=price;
    }
    public Item(String name, int price, String brand) {
        this(name, price);
        this.brand=brand;
    }
    public Item(String name, int price, String brand, String event) {
        this(name, price,brand);
        this.event=event;
    }
    public String getName() {
        return this.name;
    }
    public int getPrice() {
        return this.price;
    }
    public String getBrand() {
        return this.brand;
    }
    public String getEvent() {
        return this.event;
    }


}
