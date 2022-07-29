import java.sql.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class App {
    static final String DB_URL = "jdbc:mysql://localhost:3306/a1_091";
    static final String username = "root";
    static final String password = "123456";

    public static void main(String[] args){
        Connection conn = null;
        try{
            conn = DriverManager.getConnection(DB_URL, username, password);
        }catch(Exception ex) {
            System.err.println(ex);	
        }
        try (Scanner in = new Scanner(System.in)) {
            while (true){
                System.out.println("To execute function 1 press 1");
                System.out.println("To execute function 2 press 2");
                System.out.println("To execute function 3 press 3");
                System.out.println("To execute function 4 press 4");
                System.out.println("To quit this program enter quit");
                String select = in.nextLine();
                
                //  Function #1 to print part list
                if(select.equals("1")) {
                    ResultSet answers = function1_091(conn);
                    System.out.println("Query Return----------------------------------------------\n");
                    try {
                        while (answers.next()){
                            int no091 = answers.getInt("partNo_091");
                            int price091 = answers.getInt("currentPrice_091");
                            String name091 = answers.getString("partName_091");
                            String desc091 = answers.getString("partDescription_091");                        
                            System.out.println(no091 + ":" + desc091 + ":" + name091 + ":" + price091 +"\n");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }else if(select.equals("2")) {       
                    System.out.println("Enter your client number.....");
                    int clientnumber = Integer.parseInt(in.nextLine());
                    ArrayList<Integer> id = new ArrayList<Integer>(); 
                    ArrayList<Integer> amount = new ArrayList<Integer>(); 
                    ArrayList<Integer> price = new ArrayList<Integer>(); 
                    while(true){
                        System.out.println("What part id do you want?");
                        id.add(Integer.parseInt(in.nextLine()));
                        System.out.println("How much do you want?");
                        amount.add(Integer.parseInt(in.nextLine()));
                        System.out.println("How much do you want to pay?");
                        price.add(Integer.parseInt(in.nextLine()));
                        System.out.println("Do you want to add another part to your order?");
                        String continues = in.nextLine();
                        if(!continues.equalsIgnoreCase("yes")) {
                            break;
                        }
                    }
                    function2_091(conn,clientnumber,id,amount,price);
                } else if(select.equals("3")) {
                    System.out.println("Enter your client ID.....");
                    int id = Integer.parseInt(in.nextLine());
                    ResultSet answers = function3_091(conn, id);
                    System.out.println("Query Return----------------------------------------------\n");
                    try {
                        while (answers.next()){
                            int no091 = answers.getInt("poNo_091");
                            Date po091 = answers.getDate("datePO");
                            String status091 = answers.getString("status_091");
                            int id091 = answers.getInt("clientID_091");
                            System.out.println(no091 + ":" + po091 + ":" + status091 + ":" + id091 +"\n");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }else if(select.equals("4")) {
                    System.out.println("Enter your po number.....");
                    int po = Integer.parseInt(in.nextLine()); 
                    ResultSet answers = function4_091(conn, po);

                    System.out.println("Query Return----------------------------------------------\n");
                    try {
                        while (answers.next()){
                            int pno091 = answers.getInt("poNum_091");
                            int lno091 = answers.getInt("lineNum_091");
                            int partno091 = answers.getInt("partNum_091");
                            int price091 = answers.getInt("partPrice_091");
                            int amount091 = answers.getInt("partQuant_091");
                            // print the results
                            System.out.println(pno091 + ":" + lno091 + ":" + partno091 + ":" + price091 + ":" + amount091 +"\n");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }else if(select.equalsIgnoreCase("quit")) {
                    System.out.println("Quitting program.....");
                    System.exit(0);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet function1_091 (Connection conn){
        try {
            //Preparing statement 
            Statement st = conn.createStatement();
            ResultSet answers = st.executeQuery("Select * from part_091");
            return answers;
        }
        catch (Exception ex) {
            System.err.println(ex);
            return null;
        }
    }



    public static void function2_091 (Connection conn,int clientnumber,ArrayList id,ArrayList amount,ArrayList price){
        try{                  
            Statement st = conn.createStatement();
            ResultSet clientcheck = st.executeQuery("SELECT * FROM client_091 where clientId_091 = " + clientnumber);
            if(!clientcheck.next()){
                System.out.println("Error: Not a client");
                return;
            }

            for(int x = 0; x < id.size(); x++){
                ResultSet amountcheck = st.executeQuery("SELECT * FROM part_091 where partNo_091 = " + id.get(x));
                if(!amountcheck.next()){
                    System.out.println("Error: Wrong part ID");
                    return;
                }
                int amounthave = amountcheck.getInt("QoH_091");
                if(amounthave < (int)amount.get(x)){
                    System.out.println("Error: Can not fill that amount");
                    return;
                }
                int pricehave = amountcheck.getInt("currentPrice_091");
                if(pricehave != (int)price.get(x)){
                    System.out.println("Error: Make sure price is same as part");
                    return;
                }
            }
            LocalDateTime now = LocalDateTime.now();  
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
            ResultSet poNo = st.executeQuery("SELECT MAX(poNo_091) FROM po_091");
            int ponumber = 1;
            if(poNo.next()){
                if(ponumber <= poNo.getInt("MAX(poNo_091)")){
                    ponumber = poNo.getInt("MAX(poNo_091)")+ 1;
                }
            }
           // System.out.println("INSERT INTO po_091 (PoNo_091, datePO, status_091, clientID_091) "+"VALUES (\"" + ponumber + " , " + format.format(now) + "\", 'In progress', "+ clientnumber +")");
            st.executeUpdate("INSERT INTO po_091 (PoNo_091, datePO, status_091, clientID_091) "+"VALUES (" + ponumber + " , \"" + format.format(now) + "\", 'In progress', "+ clientnumber +")");
            for(int y = 0; y < id.size(); y++){
                //System.out.println("No errors.....");
                st.executeUpdate("INSERT INTO line_091 (lineNum_091, poNum_091, partNum_091, partPrice_091, partQuant_091) "+"VALUES (" + (y+1) + ", " + ponumber + "," + (int)id.get(y) + " , "+ (int)price.get(y) +", " + (int)amount.get(y) + ")");
            }
            return;
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        } 
    }

    public static ResultSet function3_091 (Connection conn,int id){
        try {
            //Preparing statement 
            Statement st = conn.createStatement();
            ResultSet answers = st.executeQuery("SELECT * FROM po_091 where clientId_091 = " + id);
            return answers;
        }
        catch (Exception ex) {
            System.err.println(ex);
            return null;
        }
    }

    public static ResultSet function4_091 (Connection conn,int po){
        try {
            //Preparing statement 
            Statement st = conn.createStatement();
            ResultSet answers = st.executeQuery("SELECT * FROM line_091 where poNum_091 = " + po);
            return answers;
        }
        catch (Exception ex) {
            System.err.println(ex);
            return null;
        }
    }
}
