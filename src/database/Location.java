package database;

public class Location {
    String storeID;
    String brandName;
    String storeName;
    String storeAddress;
    String placeURL;
    String distance;
    double locationX;
    double locationY;
    public Location( String storeID,String brandName, String storeName,String storeAddress, String placeURL,String distance,double locationX,double locationY) {
        this.storeID=storeID;
        this.brandName=brandName;
        this.storeName=storeName;
        this.storeAddress=storeAddress;
        this.placeURL=placeURL;
        this.distance=distance;
        this.locationX=locationX;
        this.locationY=locationY;
    }
}
