package database;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import api.KakaoAPI;
import parser.Item;
import userInterface.Menu;
import userInterface.SQL;

public class Database {
    File file;
    FileReader fr;
    BufferedReader in;
    String[] attrName;
    String[] columnsContent;
    private final static String urldb = "jdbc:postgresql://127.0.0.1:5432/postgres";
    private final static String user = "postgres";
    private final static String pw = "0223";

    public Database() throws Exception {
        // write your code here
        createTable(connect());
        insert_product();
        Menu.list = loadData();
        //KakaoAPI kakao = new KakaoAPI();
        //System.out.println(kakao.addrToCoord("127.043784", "37.279509", "1000"));
        //Clawler clawler = new Clawler();
        //clawler.clawler_main();
    }

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(urldb, user, pw);
            //System.out.println("Connected to the PostgreSQL server successfully.");

        } catch (SQLException e) {
            System.out.println("Failed to connect to the PostgreSQL Server.");
        }
        return conn;
    }


    public ArrayList<Item> loadData() throws SQLException {
        ArrayList<Item> list  = new ArrayList<>();

        Statement statement = connect().createStatement();
        list = SQL.query(statement, "select * from Product;");
        return list;
    }


    public void createTable(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();

        statement.executeUpdate("DROP TABLE IF EXISTS Client CASCADE;");
        statement.executeUpdate("DROP TABLE IF EXISTS Stroe CASCADE;");
        statement.executeUpdate("DROP TABLE IF EXISTS Applied CASCADE;");
        statement.executeUpdate("DROP TABLE IF EXISTS Product CASCADE;");

        statement.executeUpdate("create table if not exists Client(userID int,pName varchar(20),locX double precision,locY double precision,primary key(userID));");
        statement.executeUpdate("create table if not exists Store(storeID int, bName varchar(20), sName varchar(20), sAddress varchar(40), pURL varchar(40), locX double precision,locY double precision, primary key(storeID));");
        statement.executeUpdate("create table if not exists Product(pID int, bName varchar(40), pName varchar(40), price int, eName varchar(40), primary key(pID));");
        statement.executeUpdate("create table if not exists Applied(userID int, storeID int, distance double precision, primary key(userID,storeID));");
    }
    private void loadFromCSV(ArrayList<Item> list) throws IOException {
        File csv = new File("Type_All.csv");
        BufferedReader br = new BufferedReader(new FileReader(csv));
        String line = "";
        int row =0 ,i;
        br.readLine();
        while ((line = br.readLine()) != null){
            String[] splited = line.split(",", -1);
            for(int j = 0; j<splited.length;j++)
            {
                if(splited[j].contains("'")){
                    String[] temp= splited[j].split("'",-1);
                    for (int k = 0; k<temp.length;k++)
                    {
                        if(k == 0)
                        {
                            splited[j] = temp[k];
                        }
                        else
                        {
                            splited[j]+= temp[k];
                        }
                    }
                }
            }
            list.add(new Item(splited[0],Integer.valueOf(splited[1]),splited[2],splited[3]));
        }
        System.out.println("Database load complete!!");
    }

    public void insert_product() throws Exception {
        ArrayList<Item> t1_item = new ArrayList<Item>();
        Statement statement =  connect().createStatement();
        loadFromCSV(t1_item);
        for(int i = 1;i<t1_item.size();i++)
        {
            statement.executeUpdate("Insert into Product values("+(i)+",'"+t1_item.get(i).getBrand()+"','"+t1_item.get(i).getName()+"','"+t1_item.get(i).getPrice()+"','"+t1_item.get(i).getEvent()+"');");
        }

    }

}