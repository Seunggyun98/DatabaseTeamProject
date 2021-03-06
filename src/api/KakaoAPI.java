package api;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import database.Database;
import userInterface.Menu;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.crypto.Data;


public class KakaoAPI {
    public static void find(int radius, String brand, String Item_number) throws SQLException {

        JSONArray jsonArray = addrToCoord("127.043784", "37.279509", String.valueOf(radius));
        printJSONArray(jsonArray, brand, Item_number);
    }

    public static JSONArray addrToCoord(String x, String y, String radius) {
        String url = "https://dapi.kakao.com/v2/local/search/category.json?category_group_code=CS2&x=" + x + "&y=" + y + "&radius=" + radius + "&sort=distance";
        JSONArray jsonArray = getJSONData(url);
        String jsonString = "";
        if (jsonArray != null) {
            jsonString = jsonArray.toString();
        }

        try {
            FileWriter output = new FileWriter("Store.json");
            output.write(jsonString);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static JSONArray getJSONData(String apiUrl) {
        StringBuilder jsonString = new StringBuilder("");
        String buf;
        String apikey = "2a05839864082b8e89f3560914b27b7f"; //apikey
        try {
            URL url = new URL(apiUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            String auth = "KakaoAK " + apikey;
            conn.setRequestMethod("GET");
            conn.setRequestProperty("X-Requested-With", "curl");
            conn.setRequestProperty("Authorization", auth);
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), StandardCharsets.UTF_8));
            while ((buf = br.readLine()) != null) {
                jsonString.append(buf);
            }
            br.close();
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(jsonString.toString());
            JSONArray parse_documents = (JSONArray) obj.get("documents");

            return parse_documents;
        } catch (Exception e) {
            System.err.println("KakaoAPI parsing failed");
            e.printStackTrace();
        }
        return null;
    }

    public static String brand_rename(String brand) {
        String renamed_brandName = "";
        switch (brand) {
            case "미니스톱":
                renamed_brandName = "MINISTOP(미니스톱)";
                break;
            case "GS25":
                renamed_brandName = "GS25(지에스25)";
                break;
            case "CU":
                renamed_brandName = "CU(씨유)";
                break;
            case "이마트24":
                renamed_brandName = "EMART24(이마트24)";
                break;
            case "세븐일레븐":
                renamed_brandName = "7-ELEVEN(세븐일레븐)";
                break;

        }
        return renamed_brandName;
    }

    public static void printJSONArray(JSONArray arr, String brand, String Item_number) throws SQLException {
        Statement statement = Database.connect().createStatement();
        ResultSet rs;
        statement.executeUpdate("delete from Store *;");


        JSONObject store;
        System.out.println("Store List");
        for (Object parse_document : arr) {
            store = (JSONObject) parse_document;
            String storeID, brandName, storeName, storeAddress, placeURL, distance;
            double locationX, locationY;
            brandName = (String) store.get("category_name");
            if (brandName.length() > 14) {
                storeID = (String) store.get("id");
                brandName = brandName.substring(14);  // store brand
                storeName = (String) store.get("place_name");
                storeAddress = (String) store.get("road_address_name");
                placeURL = (String) store.get("place_url");
                distance = (String) store.get("distance");
                locationX = Double.valueOf((String) store.get("x"));
                locationY = Double.valueOf((String) store.get("y"));

                brandName = brand_rename(brandName);
//                String query = "insert into path values('"+Item_number+"','"+brandName+"','"+storeName+"','"+locationX+"','"+locationY+"','"+distance+"');";
//                statement.executeUpdate(query);
                statement.executeUpdate("Insert Into Store values('" + storeID + "','" + brandName + "','" + storeName + "','" + storeAddress + "','" + placeURL + "','" + locationX + "','" + locationY + "','" + distance + "');");
            }
        }
        String query = "Select sName, distance, bName, locx, locy from store";
        if (brand != null) {
            String query2 = " Where bName like concat('%','" + brand + "', '%')";
            query = query.concat(query2);
        } else {
            query = query.concat(";");
        }

        rs = statement.executeQuery(query);
        while (rs.next()) {
            System.out.println(rs.getString(1) + "(" + rs.getInt(2) + "m)");
            if (Item_number != null) {
                String query3 = "insert into path values('" + Item_number + "','" + rs.getString(3) + "','" + rs.getString(1) + "','" + rs.getDouble(4) + "','" + rs.getDouble(5) + "','" + rs.getInt(2) + "');";
                statement.executeUpdate(query3);
            }

        }
    }

}