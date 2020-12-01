package userInterface;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.postgresql.util.PSQLException;

import database.Location;
import parser.Item;

public class SQL {
    public static ArrayList<Item> query(Statement statement, String sql) throws SQLException {
        ArrayList<Item> list = new ArrayList<>();
        try {
            ResultSet resultSet = statement.executeQuery(sql);

            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            resultSet.next();
            while (resultSet.next()) {
                String pName="";
                String bName="";
                int price=0;
                String eName="";
                for (int i = 1; i <= columnsNumber; i++) {
                    switch(i) {
                        case 1:
                            break;
                        case 2:
                            bName = resultSet.getString(i);
                            break;
                        case 3:
                            pName = resultSet.getString(i);
                            break;
                        case 4:
                            price = resultSet.getInt(i);
                            break;
                        case 5:
                            eName = resultSet.getString(i);
                            break;
                    }
                }
                Item temp = new Item(pName,price,bName,eName);

                list.add(temp);
            }
        }catch(PSQLException e) {
            System.out.println("검색 결과가 없습니다.");
        }
        return list;
    }


    public static ArrayList<Location> locationQuery(Statement statement, String sql) throws SQLException {
        ArrayList<Location> location = new ArrayList<>();
        ResultSet resultSet = statement.executeQuery(sql);
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnsNumber = rsmd.getColumnCount();
        resultSet.next();
        while (resultSet.next()) {
            String storeID="";
            String brandName="";
            String storeName="";
            String storeAddress="";
            String placeURL="";
            String distance="";
            double locationX=0;
            double locationY=0;
            for (int i = 1; i <= columnsNumber; i++) {
                switch(i) {
                    case 1:
                        //eName = resultSet.getString(i);
                        storeID = resultSet.getString(i);
                        break;
                    case 2:
                        brandName = resultSet.getString(i);
                        break;
                    case 3:
                        storeName = resultSet.getString(i);
                        //price = resultSet.getString(i);
                        break;
                    case 4:
                        storeAddress = resultSet.getString(i);
                        //eName = resultSet.getString(i);
                        break;
                    case 5:
                        placeURL = resultSet.getString(i);
                        break;
                    case 6:
                        distance = resultSet.getString(i);
                        break;
                    case 7:
                        locationX = Double.valueOf(resultSet.getString(i));
                        break;
                    case 8:
                        locationY = Double.valueOf(resultSet.getString(i));
                        break;
                }
            }
            Location tmp = new Location(storeID,brandName,storeName,storeAddress,placeURL,distance,locationX,locationY);
            location.add(tmp);
        }
        return location;
    }

    public static ArrayList<Item> SortByPrice(Statement statement,String query) throws SQLException{
        ArrayList<Item> sorted = new ArrayList<>();
        System.out.println(query+" order by price asc;");
        sorted = query(statement, query+" order by price asc;");
        return sorted;
    }
    public static ArrayList<Item> SortByPriceDesc(Statement statement,String query) throws SQLException{
        ArrayList<Item> sorted = new ArrayList<>();
        sorted = query(statement, query+" order by price desc");
        return sorted;

    }

    public static int numItem (String brand){
        //편의점 별로 아이템 개수
        int num=0;
        //편의점 별로 뷰 생성  bName like concat

        return num;
    }
}
