package userInterface;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.sql.SQLException;
import java.sql.Statement;

import parser.Item;
import parser.Parser;
import api.KakaoAPI;
import database.Database;

import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Menu {
    static int searchTime = 0;
    static ArrayList<String> itemList = new ArrayList<>();
    static  HashSet<String> itemSet = new HashSet<>();
    static Tool function = new Tool();
    static Scanner in = new Scanner(System.in);
    public static ArrayList<String> itemNumber = new ArrayList<>();
    public static ArrayList<Item> list = new ArrayList<>();
    public static ArrayList<Item> found = new ArrayList<>();
    public static Statement statement;

    public static void main(String[] args) throws Exception {
        updateTable(args);

        Database database = new Database();

        statement = Database.connect().createStatement();
        System.out.println(list.size());
        System.out.println("편의점 행사상품 검색 프로그램입니다.");
        menu();

    }

    public static int recursive() throws SQLException {
        String min_distance = "";
        String max_distance = "";
        String query1 = "select sname \n" +
                "from path\n" +
                "where distance = (select min(distance) from path);";
        ResultSet rs1 = statement.executeQuery(query1);


        while (rs1.next()) {
            min_distance = rs1.getString(1);
        }

        if (itemList.size() == 0) {
            return 0;
        } else if (min_distance == "") {

            return 2;
        } else {

            ResultSet rs2;
            String query = "select sname\n" +
                    "from path\n" +
                    "where distance  = (select max(distance)from path where sname not like concat('%','" + min_distance.charAt(0) + "','%'));";
            rs2 = statement.executeQuery(query);
            /*while (rs2.next()) {
                max_distance = rs2.getString(1);
            }*/
            statement.executeUpdate("create view path_min as select * from path a where distance <= all (select distance from path b where a.itemnumber = b.itemnumber); ");

            statement.executeUpdate("create view path_number as select t3.sName as dest, t4.sName as origin, sqrt( power(t3.locx - t4.locx,2) +power(t3.locy - t4.locy,2))*1000 as weight\n" +
                    "from path_min t3 cross join path_min t4\n" +
                    "where t3.sName <> t4.sName;");

            String query3 = "with recursive route(dest,mid,origin,total,count_temp,length) as (select dest, dest || '' as mid,origin,weight as total ,1,1 from path_number union select a.dest, mid ||' -> ' || d.dest as mid ,d.origin,weight + total as total,1,a.length +1 as length from route a , path_number d where a.length < "+itemList.size()+"  and mid not like concat('%',substring(d.dest,1,3),'%')) select mid from route where dest = '"+min_distance+"' and length >= (select count(distinct substring(itemnumber,1,3)) from path a);";
            ResultSet rs3 = statement.executeQuery(query3);
            while (rs3.next()) {
                System.out.println(rs3.getString(1));
                break;
            }
            statement.executeUpdate("drop view path_number;");
            statement.executeUpdate("drop view path_min;");
            return 1;
        }
    }

    public static void menu() throws FileNotFoundException, SQLException {


        System.out.println("--------------메뉴--------------");
        System.out.println("1.상품 검색\t2.전체 상품 리스트\t3.주변 편의점 목록\t4.검색기록\t5.즐겨찾기\t6.프로그램 종료");
        try {
            int menuSelect = in.nextInt();
            switch (menuSelect) {
                case 1:
                    System.out.println("상품을 검색합니다.");
                    searchItemMenu();
                    break;
                case 2:
                    showEntryStatistic();
                    showEntryList();
                    break;

                case 3:
                    System.out.println("몇 m 반경 내에 있는 편의점을 확인하시겠습니까? : ");
                    int radius = in.nextInt();
                    System.out.println(radius + "m 내의 편의점 목록을 보여줍니다.");
                    KakaoAPI.find(radius, null, null);
                    menu();
                    break;
                case 4:
                    showHistory();
                    menu();
                    break;
                case 5:
                    System.out.println("검색하신 기록을 보여드립니다.");
                    for (String list_name : itemList) {
                        System.out.println(list_name);
                    }
                    System.out.println();
                    System.out.println("검색하신 기록을 통해 만들어진 편의점 경로를 보시겠습니까?");
                    System.out.println("1.예\t2.아니요");
                    int Select = in.nextInt();
                    switch (Select) {
                        case 1:
                            int flag = recursive();
                            if (flag == 0) {
                                System.out.println("등록된 상품이 없습니다.");
                            } else if (flag == 2) {
                                System.out.println("저장된 편의점이 없습니다.");
                            }
                            menu();
                            break;
                        case 2:
                            menu();
                            break;
                    }
                    break;
                case 6:
                    System.out.println("프로그램을 종료합니다. 좋은 하루 되세요~");
                    System.exit(0);
                
                default:
                    menu();
                    break;

            }
        } catch (InputMismatchException e) {
            menu();
        }
    }


    private static void showEntryList() throws FileNotFoundException, SQLException {
        //20개씩 보여주기, 1~maxPage까지 선택으로 리스트 갱신, 0입력시 메뉴로
        ArrayList<Item> found = list;
        String brand = function.searchBrand();
        /*
         * KakaoAPI.find(radius); 주변 편의점 테이블 저장.
         * list = SQL.query(주변 편의점 테이블 natural join ItemView 테이블);
         *
         */

        System.out.println("검색 필터를 설정해주세요.");
        System.out.println("1. 기본 정렬 2. 가격 오름차순 3. 가격 내림차순 4. 행사별 5. 주변 편의점");
        String query = "", query1 = "", query2 = "";

        if (brand.equals("all")) {
            query1 = "Select pID, bName, pName, price, eName From Product";
        } else {

            query1 = "Select pID, bName, pName, price, eName From Product ";
            query2 = "Where bName like concat('%','" + brand + "', '%')";
        }
        query = query1 + query2;

        int filter = in.nextInt();
        switch (filter) {
            case 1:
                found = SQL.query(statement, query);
                break;
            case 2:
                found = SQL.SortByPrice(Menu.statement, query);
                break;
            case 3:
                found = SQL.SortByPriceDesc(statement, query);
                break;
            case 4:
                found = function.searchEvent("all", brand);
                break;
            case 5:

                System.out.println("몇 m 반경 내에 있는 편의점을 확인하시겠습니까? : ");
                int radius = in.nextInt();
                System.out.println(radius + "m 내의 편의점 목록을 보여줍니다.");
                KakaoAPI.find(radius, null, null);
                query = query1 + " natural join store " + query2;
                //System.out.println(query);
                System.out.println("해당 편의점에서 판매하는 상품 목록입니다.");
                found = SQL.query(statement, query);
                //list = SQL.query(statement, "Select pID, bName, pName, price, eName From  ");
                break;
            default:
                System.out.println("다시 선택해주세요.");
                showEntryList();
                break;
        }

        System.out.println("총 상품 개수 :" + found.size());
        int idx = 0;
        int next = -1;
        for (; ; ) {
            if (next == -1) {
                System.out.println("-------------------------------------------상품목록-------------------------------------------------");
                System.out.println((next + 2) + " 페이지/" + ((found.size() / 20) + 1) + "페이지");
                System.out.println("----------------------------------------------------------------------------------------------------");
            } else {
                System.out.println();
                System.out.println("-------------------------------------------상품목록-------------------------------------------------");
                System.out.println((next) + " 페이지/" + ((found.size() / 20) + 1) + "페이지");
                System.out.println("----------------------------------------------------------------------------------------------------");
            }
            System.out.printf("\t%-3.15s\t\t%-3.15s\t\t\t%s\t\t%s\n", "편의점", "상품명", "가격", "행사");
            System.out.println("----------------------------------------------------------------------------------------------------");
            for (int i = idx; i < idx + 20; i++) {
                try {
                    System.out.printf("%-15s \t %25s \t\t\t %-8d \t%3s\n"
                            , found.get(i).getBrand(), found.get(i).getName(), found.get(i).getPrice(), found.get(i).getEvent());
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
            }
            System.out.println("-----------------------------------------------------------------------------------------------------");
            System.out.print("(메뉴 : 0) \t원하는 페이지 : ");

            try {
                next = in.nextInt();

                if (next == 0) {
                    System.out.println("메뉴화면으로 돌아갑니다.");
                    menu();
                } else if (next > ((found.size() / 20) + 1)) {

                    System.out.println("없는 페이지 입니다. 다시 검색해주세요");
                    menu();
                } else {
                    idx = (next - 1) * 20;
                    continue;
                }
            } catch (InputMismatchException e) {
                System.out.println("메뉴화면으로 돌아갑니다.");
                in = new Scanner(System.in);
                menu();
            }
        }
    }


    private static void showList(String ItemName) throws FileNotFoundException, SQLException {
        //20개씩 보여주기, 1~maxPage까지 선택으로 리스트 갱신, 0입력시 메뉴로

        String brand = function.searchBrand();
        System.out.println("-----------------------------");
        ArrayList<Item> found = new ArrayList<>();
        found = Menu.found;
        System.out.println("검색 필터를 설정해주세요.");
        System.out.println("1.기본 정렬 \t 2.가격 오름차순 \t 3.가격 내림차순 \t 4.행사별 \t5.주변 편의점");


        String query = "", query1 = "", query2 = "";

        if (brand.equals("all")) {
            query1 = "Select pID, bName, pName, price, eName From " + ItemName + "View";
        } else {
          
            query1 = "Select pID, bName, pName, price, eName From " + ItemName + "View  ";
            query2 = "Where bName like concat('%','" + brand + "', '%')";
        }
        query = query1 + query2;
        int filter = in.nextInt();
        switch (filter) {
            case 1:
                //System.out.println(ItemName+brand);
                found = SQL.query(statement, query);
                break;
            case 2:
                found = SQL.SortByPrice(Menu.statement, query);
                break;
            case 3:
                found = SQL.SortByPriceDesc(Menu.statement, query);
                break;
            case 4:
                found = function.searchEvent(ItemName, brand);
                break;
            case 5:

                System.out.println("몇 m 반경 내에 있는 편의점을 확인하시겠습니까? : ");
                int radius = in.nextInt();
                System.out.println(radius + "m 내의 편의점 목록을 보여줍니다.");
                KakaoAPI.find(radius, brand, ItemName);
                query = query1 + " natural join store " + query2;
                //System.out.println(query);
                System.out.println("해당 편의점에서 판매하는 상품 목록입니다.");
                found = SQL.query(statement, query);
                //list = SQL.query(statement, "Select pID, bName, pName, price, eName From  ");
                break;
            default:
                System.out.println("다시 선택해주세요.");
                showList(ItemName);
                break;
        }


        itemList.add(ItemName);
        statement.executeUpdate("insert into Client values(" + (searchTime++) + ",'" + ItemName + "');");
        if (found.size() != 0) {
            in = new Scanner(System.in);
            System.out.println("총 상품 개수 :" + found.size());


            int idx = 0;
            int next = -1;
            for (; ; ) {
                if (next == -1) {
                    System.out.println("-------------------------------------------상품목록-------------------------------------------------");
                    System.out.println((next + 2) + " 페이지/" + ((found.size() / 20) + 1) + "페이지");
                    System.out.println("----------------------------------------------------------------------------------------------------");
                } else {
                    System.out.println();
                    System.out.println("-------------------------------------------상품목록-------------------------------------------------");
                    System.out.println((next) + " 페이지/" + ((found.size() / 20) + 1) + "페이지");
                    System.out.println("----------------------------------------------------------------------------------------------------");
                }
                System.out.printf("\t%-15s\t\t%-15s\t\t\t%s\t\t%s\n", "편의점", "상품명", "가격", "행사");
                System.out.println("----------------------------------------------------------------------------------------------------");
                for (int i = idx; i < idx + 20; i++) {
                    try {
                        System.out.printf("%-15s \t %25s \t\t\t %-8d \t%3s\n"
                                , found.get(i).getBrand(), found.get(i).getName(), found.get(i).getPrice(), found.get(i).getEvent());
                    } catch (IndexOutOfBoundsException e) {
                        break;
                    }
                }
                System.out.println("-----------------------------------------------------------------------------------------------------");
                System.out.print("(메뉴 : 0) \t원하는 페이지 : ");

                try {
                    next = in.nextInt();
                    if (next == 0) {
                        //뷰 드랍
                        statement.executeUpdate("Drop view if exists " + ItemName + "View Cascade;");

                        System.out.println("메뉴화면으로 돌아갑니다.");
                        menu();
                    } else if (next > ((found.size() / 20) + 1)) {

                        statement.executeUpdate("Drop view if exists " + ItemName + "View Cascade;");
                        System.out.println("없는 페이지 입니다. 다시 검색해주세요");
                        menu();
                    } else {
                        idx = (next - 1) * 20;
                        continue;
                    }
                } catch (InputMismatchException e) {

                    statement.executeUpdate("Drop view " + ItemName + "View Cascade;");
                    System.out.println("메뉴화면으로 돌아갑니다.");
                    in = new Scanner(System.in);
                    menu();
                }
            }

        }
        statement.executeUpdate("Drop view " + ItemName + "View Cascade;");
        System.out.println("메뉴화면으로 돌아갑니다.");
        menu();
    }

    private static void searchItemMenu() throws SQLException {
        //이름으로 상품 검색. 문자열 포함하는 모든 상품 보여주기. 0을 입력하면 메뉴로
        in = new Scanner(System.in);
        System.out.println("검색 필터를 설정해주세요.");
        System.out.println("1. 상품명\t\t0. 메인 메뉴");
        try {
            int selectMenu = in.nextInt();
            switch (selectMenu) {
                case 1:
                    String ItemName = function.searchName(list);
                    itemNumber.add(ItemName);
                    showList(ItemName);
                    break;
                case 0:
                    System.out.println("메인 메뉴로 돌아갑니다.");
                    try {
                        menu();
                    } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                default:
                    System.out.println("다시 입력해주세요.");
                    searchItemMenu();
            }
        } catch (InputMismatchException e) {
            System.out.println("입력은 숫자입니다. 다시 시도해주세요.");
            searchItemMenu();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
    private static void showEntryStatistic() throws SQLException {
            String query = "select bName,eName,count(*) from Product group by rollup(bName, eName);";
            ResultSet resultSet = statement.executeQuery(query);
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
        while (resultSet.next()) {
            String bName = "";
            String eName = "";
            int cnt = 0;
            for (int i = 1; i <= columnsNumber; i++) {
                switch (i) {
                    case 1:
                        bName = resultSet.getString(i);
                        break;
                    case 2:
                        eName = resultSet.getString(i);
                        break;
                    case 3:
                        cnt = resultSet.getInt(i);
                        break;

                }
            }
            System.out.printf("%-20s\t\t%3s\t\t%4d\n",bName, eName, cnt);
            // System.out.println(ItemName+"\t\t"+bName+"\t\t"+eName+"\t\t"+cnt);
        }
    }
    private static void showHistory() throws SQLException {
        //OLAP 쿼리 사용, 뷰이용
        statement.executeUpdate("create view HistoryStatView as select S.pName as ItemName, bName, eName, count(*) " +
                "from Product I, Client S " +
                "where I.pName like concat ('%',S.pName,'%') " +
                "group by S.pName, rollup(bName, eName) " +
                "order by ItemName, bName;");

        ResultSet resultSet;
        //OLAP을 통해 얻은 결과가 너무 많기 때문에,
        // 전부 보여주지않고, 깔끔하게 보여주기 위하여 편의점 별, 행사별로만 보여줌.

        resultSet = statement.executeQuery("select *\n" +
                "  from HistoryStatView\n" +
                "  order by ItemName, bName,eName;");

        ResultSetMetaData rsmd = resultSet.getMetaData();
        int columnsNumber = rsmd.getColumnCount();

        System.out.println("ItemName\t\t\tBrand\t\t\t\tEvent\t\t\tCount");
        while (resultSet.next()) {
            String ItemName = "";
            String bName = "";
            String eName = "";
            int cnt = 0;
            for (int i = 1; i <= columnsNumber; i++) {
                switch (i) {
                    case 1:
                        ItemName = resultSet.getString(i);
                        break;
                    case 2:
                        bName = resultSet.getString(i);
                        break;
                    case 3:
                        eName = resultSet.getString(i);
                        break;
                    case 4:
                        cnt = resultSet.getInt(i);
                        break;

                }
            }
            System.out.printf("%10s\t\t%-20s\t\t%3s\t\t%4d\n", ItemName, bName, eName, cnt);
            // System.out.println(ItemName+"\t\t"+bName+"\t\t"+eName+"\t\t"+cnt);
        }
        statement.executeUpdate("drop view HistoryStatView Cascade");
    }

    private static void updateTable(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        String date = format.format(new Date());
        String day = date.split("/")[2];

        System.out.println(date);
        if (day.equals("01") || day.equals("15")) {
            Path path = Paths.get("history.txt");
            File file = new File(path.toUri());
            Scanner sc = new Scanner(file);

            String line = sc.next();
            //System.out.println(line);
            if (!line.equals(date)) {
                FileWriter fw = new FileWriter("history.txt", false);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(date);
                bw.flush();
                bw.close();
                Parser.main(args);
                Database db = new Database();
                db.createTable(Database.connect());
                db.insert_product();
                System.out.println("DB 업데이트가 완료되었습니다.");
            } else {
                System.out.println("최신 DB를 사용중입니다.");
            }
        }else {
            System.out.println("최신 DB를 사용중입니다.");
        }
    }

}

