package userInterface;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

import api.KakaoAPI;
import parser.Item;

interface ToolInterface {
    public String searchName(ArrayList<Item> list);
    public ArrayList<Item> searchEvent(String ItemName,String brand);
    public String searchBrand();
    public ArrayList<Item> searchClosest(ArrayList<Item> list);
    public ArrayList<Item> sortPrice(ArrayList<Item> list);
}

class ToolClass implements ToolInterface{

    @Override
    public String searchName(ArrayList<Item> list){
        return null;
    }

    @Override
    public ArrayList<Item> searchEvent(String ItemName,String brand) {
        return null;
    }

    @Override
    public String searchBrand() {
        return null;
    }

    @Override
    public ArrayList<Item> searchClosest(ArrayList<Item> list) {
        return null;
    }

    @Override
    public ArrayList<Item> sortPrice(ArrayList<Item> list) {
        return null;
    }

}
class Tool extends ToolClass{
    Scanner in = new Scanner(System.in);
    @Override
    public String searchName(ArrayList<Item> list){
        String ItemName="";
        try {

            do{
                System.out.println("찾고싶은 상품명을 입력해주세요 : ");
                ItemName =  in.next();
                if(ItemName.length()<2) {
                    System.out.println("두 글자 이상을 입력해주세요.");

                }
            }while(ItemName.length()<2);


            Menu.statement.executeUpdate("create view " +ItemName+"View as Select pID, bName, pName, price, eName From Product Where pName like concat('%','"+ItemName+"','%');");

            Menu.found =SQL.query(Menu.statement,
                    "Select pID, bName, pName, price, eName From Product Where pName like concat('%','"+ItemName+"', '%');");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ItemName;
    }

    @Override
    public ArrayList<Item> searchEvent(String ItemName,String brand) {
        System.out.println(brand);
        if(ItemName.equals("all")) {
            ItemName="Product";
        }else {
            ItemName=ItemName+"View";
        }
        while(true) {
            try{
                int toFind;

				/*if(viewName.equals("all")) table = "Product";
				else table = viewName;*/
                String query="";
                System.out.println("행사로 검색합니다. 찾고싶은 행사를 선택해주세요.");
                System.out.println("1. 1+1\t2. 2+1\t3. 3+1");
                toFind = in.nextInt();

                switch(toFind) {
                    case 1:
                        System.out.println("1+1행사 상품을 검색합니다.");
                        query = "Select pID, bName, pName, price, eName From "+ ItemName+" Where eName like concat('%','1+1','%')";
                        break;

                    case 2:
                        System.out.println("2+1행사 상품을 검색합니다.");
                        query ="Select pID, bName, pName, price, eName From "+ ItemName+" Where eName like concat('%','2+1','%')";
                        break;
                    case 3:
                        System.out.println("3+1행사 상품을 검색합니다.");
                        query = "Select pID, bName, pName, price, eName From "+ ItemName+" Where eName like concat('%','3+1','%')";
                        break;
                    default:

                        System.out.println("다시 선택해주세요.");
                        break;
                }

                if(brand.equals("all")) {
                    Menu.found = SQL.query(Menu.statement, query);
                }else {
                    Menu.found =SQL.query(Menu.statement,
                            query+ " and bName  like concat('%','"+brand+"','%')");

                }
                return Menu.found;
            }catch(InputMismatchException e) {

                System.out.println("다시 선택해주세요.");
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public String searchBrand() {
        int toFind=-1;
        while(toFind==-1) {
            System.out.println("편의점을 선택해주세요.(1.미니스톱\t2.GS25\t3.CU\t4.이마트24\t5.세븐일레븐)\n모든 편의점의 상품을 보시려면 '0'을 입력해주세요.");
            try{
                toFind =  in.nextInt();
            }catch(InputMismatchException e) {
                System.out.println("1~5로 입력해주세요.");
                searchBrand();
            }
            switch(toFind) {
                case 1:
                    /*
                     *Qeury이용해 찾기
                     */
                    System.out.println("미니스톱에서 검색합니다.");
                    return "미니스톱";
                case 2:
                    /*
                     *Qeury이용해 찾기
                     */
                    System.out.println("GS25에서 검색합니다.");
                    return "GS25";
                case 3:
                    /*
                     *Qeury이용해 찾기
                     */
                    System.out.println("CU에서 검색합니다.");
                    return "CU";
                case 4:
                    /*
                     *Qeury이용해 찾기
                     */
                    System.out.println("이마트24에서 검색합니다.");
                    return "이마트24";
                case 5:
                    /*
                     *Qeury이용해 찾기
                     */
                    System.out.println("세븐일레븐에서 검색합니다.");
                    return "세븐일레븐";
                case 0:
                    System.out.println("전체 편의점 목록에서 검색합니다.");
                    return "all";
                default:
                    toFind=-1;
                    System.out.println("다시 선택해주세요.");
                    break;
            }
        }
        return "all";

    }

    @Override
    public ArrayList<Item> searchClosest(ArrayList<Item> list) {
        ArrayList<Item> found = new ArrayList<>();
        int radius=1000;
        System.out.println("몇 미터 이내에 있는 편의점을 찾으시겠습니까?(기본 : 1000m) : ");
        radius = in.nextInt();
        try {
            KakaoAPI.find(radius,null);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return found;
    }
}