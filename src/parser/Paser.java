package parser;

import java.util.ArrayList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;


public class Paser {
    public static void main(String[] args) throws Exception{

        //type1 = 1+1상품
        type1_parser type1 = new type1_parser();
        ArrayList<String> t1_name = new ArrayList<>();
        ArrayList<String> t1_price_string = new ArrayList<>();
        ArrayList<Integer> t1_price = new ArrayList<>();
        ArrayList<String> t1_brand = new ArrayList<>();
        ArrayList<String> t1_event = new ArrayList<>();
        ArrayList<Item> t1_item = new ArrayList<>();
        t1_name = type1.get_name();
        t1_price_string = type1.get_price();
        t1_brand = type1.get_brand();
        t1_event = type1.get_event();


        for(int i = 0;i<t1_price_string.size();i++) {
            String[] temp = t1_price_string.get(i).split("원");
            t1_price.add(Integer.valueOf(temp[0]));
        }

        int size = t1_price.size();
        for(int i=0;i<size;i++) {
            t1_item.add(new Item(t1_name.get(i),t1_price.get(i),t1_brand.get(i), t1_event.get(i)));
        }

        saveCSV(t1_item,size,"Type_All");


    }
    public static void saveCSV(ArrayList<Item> item, int size, String fname) throws IOException {
        //csv파일로 저장
        fname=fname+".csv";
        File file = new File(fname);
        //파일 출력 한글 인코딩
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        OutputStreamWriter OutputStreamWriter = new OutputStreamWriter(fileOutputStream, "MS949");
        BufferedWriter bw = new BufferedWriter(OutputStreamWriter);

        bw.write("품목명,가격,브랜드,행사");
        bw.newLine();
        for(int i=0;i<size;i++) {
            bw.write(item.get(i).getName()+","+item.get(i).getPrice()+","+item.get(i).getBrand()+","+item.get(i).getEvent());
            bw.newLine();
        }
        System.out.println("-------------------");
        System.out.println("File Write Complete");
        System.out.println("-------------------");
        bw.close();

    }
}

class URL{
    private String URL_L = "";
    private String URL_R= "";
    public int i=0;

    public URL(String lURL ,String rURL){
        this.URL_L=lURL;
        this.URL_R=rURL;
    }
    public String next_page(){
        String l = URL_L;
        String r = URL_R;
        String new_url = "";
        i++;
        new_url = l + i + r;
        return new_url;
    }
    public String getFirstURL() {
        return URL_L+"1"+URL_R;
    }
}

class type1_parser{
    URL getURL = new URL("https://pyony.com/search/?page=","&event_type=&category=&item=&sort=&q=");
    //이름과 가격을 저장해주기 위한 ArrayList
    ArrayList<String> name=new ArrayList<>();
    ArrayList<String> price=new ArrayList<>();
    ArrayList<String> brand = new ArrayList<>();
    ArrayList<String> event = new ArrayList<>();

    //가격을 파싱함
    public ArrayList<String> get_price() throws Exception {
        getURL.i=0;//페이지 넘기기위한 변수
        Document page=Jsoup.connect(getURL.getFirstURL()).get();
        String maxPage = page.select("a.page-link").last().attr("abs:href");
        String url=getURL.next_page();

        //while문을 통해 maxpage 전까지 페이지 넘겨가며 parsing
        while(!maxPage.equals(url)) {
            Document next=Jsoup.connect(url).get();
            Elements prices = next.select(".fa.fa-coins.text-warning.pr-1");
            for(Element price : prices)
            {
                Node node = price.nextSibling();

                //csv파일에 저장을 위해 가격의 콤마(,)를 제거해줌
                String tmp = node.toString();
                StringBuilder tmp2 = new StringBuilder();

                for(int i=0;i<tmp.length();i++) {
                    if(',' != tmp.charAt(i)&&' '!=tmp.charAt(i)) {
                        tmp2.append(tmp.charAt(i));
                    }
                }
                this.price.add(tmp2.toString());
            }
            url=getURL.next_page();
        }

        Document next=Jsoup.connect(url).get();
        Elements prices = next.select(".fa.fa-coins.text-warning.pr-1");
        //마지막페이지는 페이지를 꽉 채우지 않을 수 있으므로 따로 처리해줌.
        for(Element price : prices)
        {
            Node node = price.nextSibling();
            //csv에 저장하기위해 콤마 삭제
            String tmp = node.toString();
            StringBuilder tmp2 = new StringBuilder();

            for(int i=0;i<tmp.length();i++) {
                if(',' != tmp.charAt(i)&&' '!=tmp.charAt(i)) {
                    tmp2.append(tmp.charAt(i));
                }
            }
            this.price.add(tmp2.toString());
        }

        return this.price;
    }

    //이름을 파싱함
    public ArrayList<String> get_name() throws Exception {
        getURL.i=0;
        Document page=Jsoup.connect(getURL.getFirstURL()).get();
        String maxPage = page.select("a.page-link").last().attr("abs:href");
        String url=getURL.next_page();

        int idx = 0;
        while(!maxPage.equals(url)) {
            Document next=Jsoup.connect(url).get();

            Elements names = next.select(".fa.fa-coins.text-warning.pr-1");
            for(Element e : names) {
                String tmp = e.previousElementSiblings().text().toString();
                StringBuilder tmp2 = new StringBuilder();

                for(int i=0;i<tmp.length();i++) {
                    if(',' != tmp.charAt(i)&&' '!=tmp.charAt(i)) {
                        tmp2.append(tmp.charAt(i));
                    }
                }
                this.name.add((idx++),tmp2.toString());
            }
            url=getURL.next_page();
        }

        Document next=Jsoup.connect(url).get();
        Elements names = next.select(".fa.fa-coins.text-warning.pr-1");
        for(Element e : names) {
            String tmp = e.previousElementSiblings().text().toString();
            StringBuilder tmp2 = new StringBuilder();

            for(int i=0;i<tmp.length();i++) {
                if(',' != tmp.charAt(i)&&' '!=tmp.charAt(i)) {
                    tmp2.append(tmp.charAt(i));
                }
            }
            this.name.add((idx++),tmp2.toString());
        }

        return this.name;
    }

    //브랜드명을 파싱함
    public ArrayList<String> get_brand() throws IOException{
        getURL.i=0;
        Document page=Jsoup.connect(getURL.getFirstURL()).get();
        String maxPage = page.select("a.page-link").last().attr("abs:href");
        String url=getURL.next_page();
        int idx = 0;
        while(!maxPage.equals(url)) {
            Document next=Jsoup.connect(url).get();
            Elements element = next.select(".col-md-6");
            for(Element e : element) {
                this.brand.add(idx++,e.select("small").first().text());
            }
            url=getURL.next_page();
        }

        Document next=Jsoup.connect(url).get();
        Elements brand = next.select(".col-md-6");
        for(Element e : brand) {
            this.brand.add(idx++,e.select("small").first().text());

        }

        return this.brand;
    }

    //행사명을 파싱함
    public ArrayList<String> get_event() throws IOException{
        getURL.i=0;
        Document page=Jsoup.connect(getURL.getFirstURL()).get();
        String maxPage = page.select("a.page-link").last().attr("abs:href");
        String url=getURL.next_page();

        int idx = 0;
        while(!maxPage.equals(url)) {
            Document next=Jsoup.connect(url).get();
            Elements event = next.select(".badge.bg-dark.text-white");
            for(Element e : event) {
                this.event.add(idx++,e.previousElementSibling().previousElementSibling().text());
            }
            url=getURL.next_page();
        }

        Document next=Jsoup.connect(url).get();

        Elements event = next.select(".badge.bg-dark.text-white");
        for(Element e : event) {
            this.event.add(idx++,e.previousElementSibling().previousElementSibling().text());
        }
        return this.event;

    }

}
