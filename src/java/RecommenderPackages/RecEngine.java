/*
 * this is the implementation of the Recommender Engine  Application V28
 */
package RecommenderPackages;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class RecEngine {
    private int InputNum;
    private int ReturnNum1;
    private int ReturnNum2;
    private String Return1[];
    private int Return2[];
    private int Return1ArrayIndex[][];
    private int Return2ArrayIndex[][];
    private String Return1Array[][][];
    private int Return2Array[][][];
    private float Return1ArrayWeight[][][];
    private int Return2ArrayWeight[][][];
    private int StandardWeight1[];
    private int StandardWeight2[];
    private int SeriesIdByTotalWeightIndex;
    private float SeriesIdByTotalWeight[][];
    private int NumberOfRecommendations;
    private String Recommendations[][];
    private int SearchType;
    float MultipleAddOnvalue;
    int SearchStrength;
    
    
    public RecEngine(){
        ReturnNum1=9;
        ReturnNum2=6;
        Return1 = new String[ReturnNum1];
        Return2 = new int[ReturnNum2];
        StandardWeight1 = new int[ReturnNum1];
        StandardWeight2 = new int[ReturnNum2];
        SeriesIdByTotalWeightIndex=0;
        SeriesIdByTotalWeight=new float[37000][2];
    }
    
    public String[][] GetRecommendations(String a[],int b,int c,int d,String e[],String f[]){//
        SearchStrength=d;
        SearchType=b;
        
        if (SearchType==0){
            String tempString[]=AddArrays(a,e);
            InputArray(tempString);
        }
        else if (SearchType==1){
            String Artist[] = ArtistPreSearch(a);
            String tempString[]=AddArrays(Artist,e);
            InputArray(tempString);
        }
        else if (SearchType==2){
            String Characters[] = CharactersPreSearch(a);
            String tempString[]=AddArrays(Characters,e);
            InputArray(tempString);
        }
        ConfigureStandardWeight(b);
        String tempNonShowString[]=AddArrays(e,f);
        SelectFromInput(tempNonShowString);
        GetHighestWeighted(c);

        return Recommendations;
    }
    
    //all following functions are private and gets mostly called from the GetRecommendations() function
    private void ConfigureStandardWeight(int a){       
        if (a==0){//for series search
            StandardWeight1[0]=1;//series_name
            StandardWeight1[1]=5;//story_script
            StandardWeight1[2]=5;//story_pencils
            StandardWeight1[3]=5;//story_inks
            StandardWeight1[4]=5;//story_colors
            StandardWeight1[5]=5;//story_letters
            StandardWeight1[6]=2;//story_characters
            StandardWeight1[7]=2;//story_genre
            StandardWeight1[8]=1;//series_format
            StandardWeight2[0]=2;//series_issue_count
            StandardWeight2[1]=2;//series_year_began
            StandardWeight2[2]=2;//series_year_ended
            StandardWeight2[3]=2;//series_publisher_id
            StandardWeight2[4]=1;//series_country_id
            StandardWeight2[5]=1;//issue_brand_id
        }
        else if (a==1){//for artist search
            StandardWeight1[0]=1;//series_name
            StandardWeight1[1]=200;//story_script
            StandardWeight1[2]=200;//story_pencils
            StandardWeight1[3]=100;//story_inks
            StandardWeight1[4]=50;//story_colors
            StandardWeight1[5]=20;//story_letters
            StandardWeight1[6]=1;//story_characters
            StandardWeight1[7]=1;//story_genre
            StandardWeight1[8]=1;//series_format
            StandardWeight2[0]=1;//series_issue_count
            StandardWeight2[1]=1;//series_year_began
            StandardWeight2[2]=1;//series_year_ended
            StandardWeight2[3]=1;//series_publisher_id
            StandardWeight2[4]=1;//series_country_id
            StandardWeight2[5]=1;//issue_brand_id
        }
        else if (a==2){//for character search
            StandardWeight1[0]=1;//series_name
            StandardWeight1[1]=1;//story_script
            StandardWeight1[2]=1;//story_pencils
            StandardWeight1[3]=1;//story_inks
            StandardWeight1[4]=1;//story_colors
            StandardWeight1[5]=1;//story_letters
            StandardWeight1[6]=100;//story_characters
            StandardWeight1[7]=1;//story_genre
            StandardWeight1[8]=1;//series_format
            StandardWeight2[0]=1;//series_issue_count
            StandardWeight2[1]=1;//series_year_began
            StandardWeight2[2]=1;//series_year_ended
            StandardWeight2[3]=1;//series_publisher_id
            StandardWeight2[4]=1;//series_country_id
            StandardWeight2[5]=1;//issue_brand_id
        }
    }
    
    
    private String[] CharactersPreSearch(String a[]){
        int SearchScope;
        if (SearchStrength<3) {
            SearchScope=1;
        }
        else if (SearchStrength<6) {
            SearchScope=2;
        }
        else{
            SearchScope=3;
        }
        
        int HighestWeight[][]=new int[a.length][SearchScope];
        int HighestWeightIndex[][]=new int[a.length][SearchScope];
        String ReturnArray[]=new String[a.length*SearchScope];
        int ReturnArrayIndex=0;
        String SeriesFromCharacters[][] = new String[a.length][100];
        int SeriesFromCharactersIndex[]=new int[a.length];
        int SeriesFromCharactersWeight[][] = new int[a.length][100];
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionUrl = "jdbc:mysql://localhost:3306/gcdComicDB";
            String connectionUrl1 = "root";
            String connectionUrl2 = "belmont";
            Connection con = DriverManager.getConnection(connectionUrl,connectionUrl1,connectionUrl2);
            Statement stmt = con.createStatement();
            
            for (int i = 0; i < a.length; i++) {
                String tempString=a[i];
                if (tempString.contains("'")){
                    tempString=tempString.replaceAll("'", "''");
                }
                String SQL="select gcdComicDB.gcd_series.name as series_name FROM gcdComicDB.gcd_story ";
                SQL+="JOIN gcdComicDB.gcd_issue ON gcdComicDB.gcd_story.issue_id=gcdComicDB.gcd_issue.id ";
                SQL+="JOIN gcdComicDB.gcd_series ON gcdComicDB.gcd_issue.series_id=gcdComicDB.gcd_series.id ";
                SQL+="where gcdComicDB.gcd_story.characters like '%"+tempString+"%' ";
                
                ResultSet rs = stmt.executeQuery(SQL);  
                while (rs.next()) {
                    int SeriesExists=0;
                    String TempID=rs.getString("series_name");
                    for (int j = 0; j < SeriesFromCharactersIndex[i]; j++) {
                        if (SeriesFromCharacters[i][j].equals(TempID)) {
                            SeriesFromCharactersWeight[i][j]++;
                            SeriesExists=1;
                        }
                    }
                    if (SeriesExists==0){
                        SeriesFromCharacters[i][SeriesFromCharactersIndex[i]]=TempID;
                        SeriesFromCharactersWeight[i][SeriesFromCharactersIndex[i]]=1;
                        SeriesFromCharactersIndex[i]++;
                    }
                    if (SeriesFromCharactersIndex[i]==SeriesFromCharacters[i].length){
                        SeriesFromCharacters[i] = (String[])expandArray(SeriesFromCharacters[i],100);
                        SeriesFromCharactersWeight[i] = (int[])expandArray(SeriesFromCharactersWeight[i],100);
                    }
                }              
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception: "+ e.toString());
        } catch (ClassNotFoundException cE) {
            System.out.println("Class Not Found Exception: "+ cE.toString());
        } 

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < SeriesFromCharactersIndex[i]; j++) {
                for (int k = 0; k < SearchScope; k++) {
                    if(SeriesFromCharactersWeight[i][j]>HighestWeight[i][k]){
                        for (int m = (SearchScope-1); m > k; m--) {
                            HighestWeight[i][m]=HighestWeight[i][(m-1)];
                            HighestWeightIndex[i][m]=HighestWeightIndex[i][(m-1)];
                        }
                        HighestWeight[i][k]=SeriesFromCharactersWeight[i][j];
                        HighestWeightIndex[i][k]=j;
                        break;
                    }
                }    
            }
        }
        for (int i = 0; i < a.length; i++) {
            for (int k = 0; k < SearchScope; k++) {
                ReturnArray[ReturnArrayIndex]=SeriesFromCharacters[i][HighestWeightIndex[i][k]];
                ReturnArrayIndex++;
            }
        }
        //for testing feedback
        /*for (int i = 0; i < ReturnArray.length; i++) {
            System.out.println(ReturnArray[i]);
        }*/
        return ReturnArray;       
    }
    
    private String[] ArtistPreSearch(String a[]){
        int SearchScope;
        
        if (SearchStrength<3) {
            SearchScope=2;
        }
        else if (SearchStrength<6) {
            SearchScope=3;
        }
        else{
            SearchScope=4;
        }
        
        
        String SeriesFromArtists[][] = new String[a.length][100];
        int SeriesFromArtistsIndex[]=new int[a.length];
        int SeriesFromArtistsWeight[][] = new int[a.length][100];
        int HighestWeight[][]=new int[a.length][SearchScope];
        int HighestWeightIndex[][]=new int[a.length][SearchScope];
        String ReturnArray[]=new String[a.length*SearchScope];
        int ReturnArrayIndex=0;
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionUrl = "jdbc:mysql://localhost:3306/gcdComicDB";
            String connectionUrl1 = "root";
            String connectionUrl2 = "belmont";
            Connection con = DriverManager.getConnection(connectionUrl,connectionUrl1,connectionUrl2);
            Statement stmt = con.createStatement();
            for (int i = 0; i < a.length; i++) {
                String tempString=a[i];
                if (tempString.contains("'")){
                    tempString=tempString.replaceAll("'", "''");
                }
                String SQL="select gcdComicDB.gcd_series.name as series_name FROM gcdComicDB.gcd_story ";
                SQL+="JOIN gcdComicDB.gcd_issue ON gcdComicDB.gcd_story.issue_id=gcdComicDB.gcd_issue.id ";
                SQL+="JOIN gcdComicDB.gcd_series ON gcdComicDB.gcd_issue.series_id=gcdComicDB.gcd_series.id ";
                SQL+="where gcdComicDB.gcd_story.script like '%"+tempString+"%' ";
                SQL+="or gcdComicDB.gcd_story.pencils like '%"+tempString+"%' ";
                SQL+="or gcdComicDB.gcd_story.inks like '%"+tempString+"%' ";
                SQL+="or gcdComicDB.gcd_story.colors like '%"+tempString+"%' ";
                SQL+="or gcdComicDB.gcd_story.letters like '%"+tempString+"%' ";
                SQL+="or gcdComicDB.gcd_story.editing like '%"+tempString+"%' ";
                
                ResultSet rs = stmt.executeQuery(SQL);  
                while (rs.next()) {
                    int SeriesExists=0;
                    String TempID=rs.getString("series_name");
                    for (int j = 0; j < SeriesFromArtistsIndex[i]; j++) {
                        if (SeriesFromArtists[i][j].equals(TempID)) {
                            SeriesFromArtistsWeight[i][j]++;
                            SeriesExists=1;
                        }
                    }
                    if (SeriesExists==0){
                        SeriesFromArtists[i][SeriesFromArtistsIndex[i]]=TempID;
                        SeriesFromArtistsWeight[i][SeriesFromArtistsIndex[i]]=1;
                        SeriesFromArtistsIndex[i]++;
                    }
                    if (SeriesFromArtistsIndex[i]==SeriesFromArtists[i].length){
                        SeriesFromArtists[i] = (String[])expandArray(SeriesFromArtists[i],100);
                        SeriesFromArtistsWeight[i] = (int[])expandArray(SeriesFromArtistsWeight[i],100);
                    }
                }              
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception: "+ e.toString());
        } catch (ClassNotFoundException cE) {
            System.out.println("Class Not Found Exception: "+ cE.toString());
        } 

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < SeriesFromArtistsIndex[i]; j++) {
                for (int k = 0; k < SearchScope; k++) {
                    if(SeriesFromArtistsWeight[i][j]>HighestWeight[i][k]){
                        for (int m = (SearchScope-1); m > k; m--) {
                            HighestWeight[i][m]=HighestWeight[i][(m-1)];
                            HighestWeightIndex[i][m]=HighestWeightIndex[i][(m-1)];
                        }
                        
                        
                        
                        HighestWeight[i][k]=SeriesFromArtistsWeight[i][j];
                        HighestWeightIndex[i][k]=j;
                        break;
                    }
                }    
            }
        }
        for (int i = 0; i < a.length; i++) {
            for (int k = 0; k < SearchScope; k++) {
                ReturnArray[ReturnArrayIndex]=SeriesFromArtists[i][HighestWeightIndex[i][k]];
                System.out.println(SeriesFromArtists[i][HighestWeightIndex[i][k]]);
                ReturnArrayIndex++;
            }
        }
        return ReturnArray;
    }
    
    
    private void InputArray(String a[]){
        InputNum=a.length;
        Return1ArrayIndex = new int[InputNum][ReturnNum1];
        Return2ArrayIndex = new int[InputNum][ReturnNum2];
        Return1Array = new String[InputNum][ReturnNum1][100];
        Return2Array = new int[InputNum][ReturnNum2][100];
        Return1ArrayWeight = new float[InputNum][ReturnNum1][100];
        Return2ArrayWeight = new int[InputNum][ReturnNum2][100];
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionUrl = "jdbc:mysql://localhost:3306/gcdComicDB";
            String connectionUrl1 = "root";
            String connectionUrl2 = "belmont";
            Connection con = DriverManager.getConnection(connectionUrl,connectionUrl1,connectionUrl2);
            Statement stmt = con.createStatement();

            for (int i = 0; i <InputNum; i++) {
                int FirstIndexUsed[] = Arrays.copyOf(Return1ArrayIndex[i], ReturnNum1);
                int FirstTester=0;
                String tempString=a[i];
                if (tempString.contains("'")){
                    tempString=tempString.replaceAll("'", "''");
                }
                String SQL0 = "select gcdComicDB.gcd_series.name as series_name, gcdComicDB.gcd_story.script as story_script,";
                SQL0 += "gcdComicDB.gcd_story.pencils as story_pencils,gcdComicDB.gcd_story.inks as story_inks,";
                SQL0 += "gcdComicDB.gcd_story.colors as story_colors,gcdComicDB.gcd_story.letters as story_letters,";
                SQL0 += "gcdComicDB.gcd_series.format as series_format,gcdComicDB.gcd_story.genre as story_genre,";
                SQL0 += "gcdComicDB.gcd_series.issue_count as series_issue_count,gcdComicDB.gcd_series.year_began as series_year_began,";
                SQL0 += "gcdComicDB.gcd_series.year_ended as series_year_ended,gcdComicDB.gcd_series.publisher_id as series_publisher_id,";
                SQL0 += "gcdComicDB.gcd_series.country_id as series_country_id,gcdComicDB.gcd_issue.brand_id as issue_brand_id,";
                SQL0 += "gcdComicDB.gcd_story.characters as story_characters FROM gcdComicDB.gcd_story ";
                SQL0 += "JOIN gcdComicDB.gcd_issue ON gcdComicDB.gcd_story.issue_id=gcdComicDB.gcd_issue.id ";
                SQL0 += "JOIN gcdComicDB.gcd_series ON gcdComicDB.gcd_issue.series_id=gcdComicDB.gcd_series.id ";
                SQL0 += "where gcdComicDB.gcd_series.name = '"+tempString+"' ;";
                
                ResultSet rs0 = stmt.executeQuery(SQL0);
                while (rs0.next()) {
                    Return1[0]=rs0.getString("series_name");
                    Return1[1]=rs0.getString("story_script");
                    Return1[2]=rs0.getString("story_pencils");
                    Return1[3]=rs0.getString("story_inks");
                    Return1[4]=rs0.getString("story_colors");
                    Return1[5]=rs0.getString("story_letters");
                    Return1[6]=rs0.getString("story_characters");
                    Return1[7]=rs0.getString("story_genre");
                    Return1[8]=rs0.getString("series_format");
                    
                    for (int j = 0; j < ReturnNum1; j++) {
                        String Temp0[]=Return1[j].split("];");
                        for (int l = 0; l < Temp0.length; l++) {
                            String Temp1[]=Temp0[l].split("]");
                            for (int m = 0; m < Temp1.length; m++) {
                                String Temp2[]=Temp1[m].split("\\[");
                                for (int n = 0; n < Temp2.length; n++) {
                                    String Temp3[]=Temp2[n].split(",");
                                    for (int o = 0; o < Temp3.length; o++) {
                                        String Temp4[]=Temp3[o].split(";");
                                        for (int p = 0; p < Temp4.length; p++) {
                                            String Temp[]=Temp4[p].split(":");
                                            for (int k = 0; k < Temp.length; k++) {
                                                int endIndex = Temp[k].indexOf("(");
                                                if (endIndex!=-1) {
                                                    Temp[k]=Temp[k].substring(0,endIndex);
                                                }
                                                Temp[k]=Temp[k].trim();
                                                if (!TestForDudString(Temp[k]) && RecArrayCheckerString(Return1ArrayIndex[i][j],Return1Array[i][j],Return1ArrayWeight[i][j],Temp[k])){
                                                    Return1Array[i][j][Return1ArrayIndex[i][j]]=Temp[k];
                                                    Return1ArrayWeight[i][j][Return1ArrayIndex[i][j]]=1;
                                                    Return1ArrayIndex[i][j]++;
                                                    if (Return1ArrayIndex[i][j]==Return1Array[i][j].length){
                                                        Return1ArrayWeight[i][j] = (float[])expandArray(Return1ArrayWeight[i][j],100);
                                                        Return1Array[i][j] = (String[])expandArray(Return1Array[i][j],100);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    if (FirstTester==0) {
                        Return2[0]=rs0.getInt("series_issue_count");
                        Return2[1]=rs0.getInt("series_year_began");
                        Return2[2]=rs0.getInt("series_year_ended");
                        Return2[3]=rs0.getInt("series_publisher_id");
                        Return2[4]=rs0.getInt("series_country_id");
                        Return2[5]=rs0.getInt("issue_brand_id");
                        
                        for (int j = 0; j < ReturnNum2; j++) {
                            int Temp=Return2[j];
                            if (RecArrayCheckerInt(Return2ArrayIndex[i][j],Return2Array[i][j],Return2ArrayWeight[i][j],Temp)){
                                Return2Array[i][j][Return2ArrayIndex[i][j]]=Temp;
                                Return2ArrayWeight[i][j][Return2ArrayIndex[i][j]]=1;
                                Return2ArrayIndex[i][j]++;
                                if (Return2ArrayIndex[i][j]==Return2Array[i][j].length){
                                    Return2ArrayWeight[i][j] = (int[])expandArray(Return2ArrayWeight[i][j],100);
                                    Return2Array[i][j] = (int[])expandArray(Return2Array[i][j],100);
                                }
                            }
                        }
                        FirstTester=1;
                    }
                }
                for (int k = 1; k < ReturnNum1; k++) {
                    if (Return1ArrayIndex[i][k]>FirstIndexUsed[k]) {
                        float copyArray[] = Arrays.copyOfRange(Return1ArrayWeight[i][k], FirstIndexUsed[k], Return1ArrayIndex[i][k]);
                        Arrays.sort(copyArray);
                        float HighestValue = copyArray[copyArray.length-1];
                        for (int j = FirstIndexUsed[k]; j < Return1ArrayIndex[i][k]; j++) {
                            Return1ArrayWeight[i][k][j]=Return1ArrayWeight[i][k][j]/HighestValue;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception: "+ e.toString());
        } catch (ClassNotFoundException cE) {
            System.out.println("Class Not Found Exception: "+ cE.toString());
        } 
    }
    
    
    private String SelectStringFromInput(String a[]){
        int NumHighWeights=0;
        if (SearchType==0){
            NumHighWeights=SearchStrength+5;
            MultipleAddOnvalue=0.001f*SearchStrength;
        }
        else if (SearchType==1){
            if (SearchStrength<2) {
                NumHighWeights=4;
            }
            else if (SearchStrength<4) {
                NumHighWeights=5;
            }
            else if (SearchStrength<5) {
                NumHighWeights=6;
            }
            else if (SearchStrength<7) {
                NumHighWeights=7;
            }
            else{
                NumHighWeights=8;
            }
            MultipleAddOnvalue=0.04f+(0.01f*SearchStrength);
        }
        
        else if (SearchType==2){
            if (SearchStrength<3) {
                NumHighWeights=1;
            }
            else if (SearchStrength<6) {
                NumHighWeights=1;
            }
            else{
                NumHighWeights=2;
            }
            MultipleAddOnvalue=0.04f+(0.01f*SearchStrength);
        }
        
        String SQL1 = "select gcdComicDB.gcd_series.id as series_id,gcdComicDB.gcd_series.name as series_name, ";
        SQL1 += "gcdComicDB.gcd_story.script as story_script,gcdComicDB.gcd_story.pencils as story_pencils,";
        SQL1 += "gcdComicDB.gcd_story.inks as story_inks,gcdComicDB.gcd_story.colors as story_colors,gcdComicDB.gcd_story.letters as story_letters,";
        SQL1 += "gcdComicDB.gcd_series.format as series_format,gcdComicDB.gcd_story.genre as story_genre,";
        SQL1 += "gcdComicDB.gcd_series.issue_count as series_issue_count,gcdComicDB.gcd_series.year_began as series_year_began,";
        SQL1 += "gcdComicDB.gcd_series.year_ended as series_year_ended,gcdComicDB.gcd_series.publisher_id as series_publisher_id,";
        SQL1 += "gcdComicDB.gcd_series.country_id as series_country_id,gcdComicDB.gcd_issue.brand_id as issue_brand_id,";
        SQL1 += "gcdComicDB.gcd_story.characters as story_characters FROM gcdComicDB.gcd_story ";
        SQL1 += "JOIN gcdComicDB.gcd_issue ON gcdComicDB.gcd_story.issue_id=gcdComicDB.gcd_issue.id ";
        SQL1 += "JOIN gcdComicDB.gcd_series ON gcdComicDB.gcd_issue.series_id=gcdComicDB.gcd_series.id where"; 

        String SqlSelect1[] = new String[ReturnNum1-2];
        String SqlSelectStringEnd1[] = new String[ReturnNum1-2];
        SqlSelect1[0]="gcdComicDB.gcd_series.name != '";
        SqlSelect1[1]="gcdComicDB.gcd_story.script like '%";
        SqlSelect1[2]="gcdComicDB.gcd_story.pencils like '%";
        SqlSelect1[3]="gcdComicDB.gcd_story.inks like '%";
        SqlSelect1[4]="gcdComicDB.gcd_story.colors like '%";
        SqlSelect1[5]="gcdComicDB.gcd_story.letters like '%";
        SqlSelect1[6]="gcdComicDB.gcd_story.characters like '%";
        SqlSelectStringEnd1[0]="'";
        SqlSelectStringEnd1[1]="%'";
        SqlSelectStringEnd1[2]="%'";
        SqlSelectStringEnd1[3]="%'";
        SqlSelectStringEnd1[4]="%'";
        SqlSelectStringEnd1[5]="%'";
        SqlSelectStringEnd1[6]="%'";
        
        if (SearchType==0) {
            SQL1+=" (";
            int TempFirstSelectOnType1=0;
            for (int j = 0; j < InputNum; j++) {
                for (int k = 0; k < Return1ArrayIndex[j][0]; k++) {
                        if(TempFirstSelectOnType1==0){
                            TempFirstSelectOnType1++;
                        }
                        else{
                            SQL1+=" and ";
                        }
                        String ExceptionTemp=Return1Array[j][0][k];
                        if (ExceptionTemp.contains("'")){
                            ExceptionTemp=ExceptionTemp.replaceAll("'", "''");
                        }
                        SQL1+=SqlSelect1[0]+ExceptionTemp+SqlSelectStringEnd1[0];
                    }
            }
            for (int i = 0; i < a.length; i++) {
                SQL1+=" and ";
                String ExceptionTemp=a[i];
                if (ExceptionTemp.contains("'")){
                    ExceptionTemp=ExceptionTemp.replaceAll("'", "''");
                }
                SQL1+=SqlSelect1[0]+ExceptionTemp+SqlSelectStringEnd1[0];
            }
            SQL1+=") and";
        }
        else if (a.length>0){
            SQL1+=" (";
            int TempFirstSelectOnType1=0;
            for (int i = 0; i < a.length; i++) {
                if(TempFirstSelectOnType1==0){
                    TempFirstSelectOnType1++;
                }
                else{
                    SQL1+=" and ";
                }
                String ExceptionTemp=a[i];
                if (ExceptionTemp.contains("'")){
                    ExceptionTemp=ExceptionTemp.replaceAll("'", "''");
                }
                SQL1+=SqlSelect1[0]+ExceptionTemp+SqlSelectStringEnd1[0];
            }
            SQL1+=") and";
        }
                
        SQL1+=" (";
        
        int TempFirstStartOfLoops1=0;
        for (int i = 1; i < 7; i++) {//ReturnNum1 7 and 8 are too general for this stage
            int CheckBackOnString=0;
            if(TempFirstStartOfLoops1==0){
                TempFirstStartOfLoops1++;
                SQL1+=" (";
            }
            else{
                SQL1+=" or (";
            }
            int TempFirstSelectOnType=0;
            for (int j = 0; j < InputNum; j++) {
                if (Return1Array[j][i][0]!=null){
                    float HighestWeight[]=new float[NumHighWeights];
                    int HighestWeightIndex[]=new int[NumHighWeights];
                    for (int k = 0; k < Return1ArrayIndex[j][i]; k++) {
                        for (int l = 0; l < NumHighWeights; l++) {
                            if (Return1ArrayWeight[j][i][k]>HighestWeight[l]) {
                                
                                for (int m = (NumHighWeights-1); m > l; m--) {
                                    HighestWeight[m]=HighestWeight[(m-1)];
                                    HighestWeightIndex[m]=HighestWeightIndex[(m-1)];
                                }
                                
                                HighestWeight[l]=Return1ArrayWeight[j][i][k];
                                HighestWeightIndex[l]=k;
                                //for testing feedback
                                /*for (int m = 0; m < NumHighWeights; m++) {
                                    System.out.println(HighestWeight[m]+" "+HighestWeightIndex[m]);
                                }*/
                                //System.out.println("=====");
                                break;
                            }
                        }
                    }
                    for (int l = 0; l < NumHighWeights; l++) {
                        if(TempFirstSelectOnType==0){
                            TempFirstSelectOnType++;
                        }
                        else{
                            SQL1+=" or ";
                        }
                        String ExceptionTemp=Return1Array[j][i][HighestWeightIndex[l]];
                        //for testing feedback
                        //System.out.println(Return1Array[j][i][HighestWeightIndex[l]]);
                        if (ExceptionTemp.contains("'")){
                            ExceptionTemp=ExceptionTemp.replaceAll("'", "''");
                        }
                        SQL1+=SqlSelect1[i]+ExceptionTemp+SqlSelectStringEnd1[i];
                    }
                }
                else{
                    j=InputNum;
                    CheckBackOnString=1;
                }
            }
            SQL1+=")";
            if (CheckBackOnString==1){
                if (SQL1.contains(" or ()")) {
                    SQL1=SQL1.replace(" or ()"," ");
                }
            }
        }
        SQL1+=")";
        //for testing feedback
        //System.out.println(SQL1);
        
        return SQL1;
    }
    
    
    private void SelectFromInput(String a[]){ 
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionUrl = "jdbc:mysql://localhost:3306/gcdComicDB";
            String connectionUrl1 = "root";
            String connectionUrl2 = "belmont";
            Connection con = DriverManager.getConnection(connectionUrl,connectionUrl1,connectionUrl2);
            Statement stmt = con.createStatement();
            
            String SQL1=SelectStringFromInput(a);
            
            ResultSet rs1 = stmt.executeQuery(SQL1);  
            while (rs1.next()) {
                Return1[1]=rs1.getString("story_script");
                Return1[2]=rs1.getString("story_pencils");
                Return1[3]=rs1.getString("story_inks");
                Return1[4]=rs1.getString("story_colors");
                Return1[5]=rs1.getString("story_letters");
                Return1[6]=rs1.getString("story_characters");
                Return1[7]=rs1.getString("story_genre");
                Return1[8]=rs1.getString("series_format");
                
                Return2[0]=rs1.getInt("series_issue_count");
                Return2[1]=rs1.getInt("series_year_began");
                Return2[2]=rs1.getInt("series_year_ended");
                Return2[3]=rs1.getInt("series_publisher_id");
                Return2[4]=rs1.getInt("series_country_id");
                Return2[5]=rs1.getInt("issue_brand_id");
                
                float Return2HighLow[]=new float [3];
                Return2HighLow[0]=0.1f;
                Return2HighLow[1]=0.002f;
                Return2HighLow[2]=0.002f;
                int TempSeriesId = rs1.getInt("series_id");
                float TempWeightToAdd=0;
                
                for (int i = 1; i < ReturnNum1; i++) {//ReturnNum1 not incuding series name (string returns)
                    for (int j = 0; j < InputNum; j++) {
                        for (int k = 0; k < Return1ArrayIndex[j][i]; k++) {
                            if (Return1[i].contains(Return1Array[j][i][k])){//.toLowerCase()
                                TempWeightToAdd+=(StandardWeight1[i]*Return1ArrayWeight[j][i][k]);
                            }
                        }
                    }
                }
                for (int i = 0; i < 3; i++) {//0-2 are the - + returns (int returns)
                    for (int j = 0; j < InputNum; j++) {
                        for (int k = 0; k < Return2ArrayIndex[j][i]; k++) {
                            if ((Return2Array[j][i][k]+(Return2Array[j][i][k]*Return2HighLow[i]))>=Return2[i] && (Return2Array[j][i][k]-(Return2Array[j][i][k]*Return2HighLow[i]))<=Return2[i]){//.toLowerCase()
                                TempWeightToAdd+=(StandardWeight2[i]*Return2ArrayWeight[j][i][k]);
                            }
                        }
                    }
                }
                for (int i = 3; i < 5; i++) {//3-5 are publisher country and brand (int returns)
                    for (int j = 0; j < InputNum; j++) {
                        for (int k = 0; k < Return2ArrayIndex[j][i]; k++) {
                            if (Return2[i]==Return2Array[j][i][k]){
                                TempWeightToAdd+=(StandardWeight2[i]*Return2ArrayWeight[j][i][k]);
                            }
                        }
                    }
                }
                int SeriesExists=0;
                for (int i = 0; i < SeriesIdByTotalWeightIndex; i++) {
                    if ((int)SeriesIdByTotalWeight[i][0]==TempSeriesId){
                        SeriesIdByTotalWeight[i][1]+=TempWeightToAdd*MultipleAddOnvalue;
                        SeriesExists=1;
                    }
                }
                if (SeriesExists==0) {
                    SeriesIdByTotalWeight[SeriesIdByTotalWeightIndex][0]=TempSeriesId;
                    SeriesIdByTotalWeight[SeriesIdByTotalWeightIndex][1]=TempWeightToAdd;
                    SeriesIdByTotalWeightIndex++;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception: "+ e.toString());
        } catch (ClassNotFoundException cE) {
            System.out.println("Class Not Found Exception: "+ cE.toString());
        } 
    }
    
    
    private void GetHighestWeighted(int a){
        if (SeriesIdByTotalWeightIndex<a){
            a=SeriesIdByTotalWeightIndex;
        }
        NumberOfRecommendations=a;
        Recommendations = new String[a][8];
        //for testing feedback
        //System.out.println(SeriesIdByTotalWeightIndex);
        /*for (int i = 0; i < SeriesIdByTotalWeightIndex; i++) {
            System.out.println(SeriesIdByTotalWeight[i][0]+" "+SeriesIdByTotalWeight[i][1]);
        }*/
        
        
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionUrl = "jdbc:mysql://localhost:3306/gcdComicDB";
            String connectionUrl1 = "root";
            String connectionUrl2 = "belmont";
            Connection con = DriverManager.getConnection(connectionUrl,connectionUrl1,connectionUrl2);
            Statement stmt = con.createStatement();
            
            float HighestWeight[]=new float[NumberOfRecommendations];
            int HighestWeightIndex[]=new int[NumberOfRecommendations];
            
            for (int i = 0; i < SeriesIdByTotalWeightIndex; i++) {
                
                for (int j = 0; j < NumberOfRecommendations; j++) {
                    if(HighestWeight[j] < SeriesIdByTotalWeight[i][1]){
                        for (int k = (NumberOfRecommendations-1); k > j; k--) {
                            HighestWeight[k]=HighestWeight[(k-1)];
                            HighestWeightIndex[k]=HighestWeightIndex[(k-1)];
                        }
                        
                        HighestWeight[j]=SeriesIdByTotalWeight[i][1];
                        HighestWeightIndex[j]=i;
                        //for testing feedback
                        /*for (int k = 0; k < NumberOfRecommendations; k++) {
                            System.out.println(HighestWeight[k]+" "+HighestWeightIndex[k]);
                        }*/
                        break;
                    }
                }
            }
            
            //for testing feedback
            /*for (int i = 0; i < NumberOfRecommendations; i++) {
                System.out.println(HighestWeightIndex[i]+" "+SeriesIdByTotalWeight[(int)HighestWeightIndex[i]][0]+" "+SeriesIdByTotalWeight[(int)HighestWeightIndex[i]][1]);
            }*/
            for (int i = 0; i < NumberOfRecommendations; i++) {
                String SQL2 = "SELECT gcdComicDB.gcd_series.name as series_name,gcdComicDB.gcd_publisher.name as publisher_name ";
                SQL2+=",gcdComicDB.gcd_series.issue_count as series_issue_count,gcdComicDB.gcd_series.year_began as series_year_began ";
                SQL2+=",gcdComicDB.gcd_series.year_ended as series_year_ended,gcdComicDB.gcd_series.format as series_format ";
                SQL2+=",gcdComicDB.gcd_series.notes as series_notes ,gcdComicDB.gcd_story.synopsis as story_synopsis FROM gcdComicDB.gcd_story ";
                SQL2+="JOIN gcdComicDB.gcd_issue ON gcdComicDB.gcd_story.issue_id=gcdComicDB.gcd_issue.id ";
                SQL2+="JOIN gcdComicDB.gcd_series ON gcdComicDB.gcd_issue.series_id=gcdComicDB.gcd_series.id ";
                SQL2+="JOIN gcdComicDB.gcd_publisher ON gcdComicDB.gcd_series.publisher_id=gcdComicDB.gcd_publisher.id ";
                SQL2+="where gcdComicDB.gcd_series.id = "+(int)SeriesIdByTotalWeight[HighestWeightIndex[i]][0]+" ;"; 
                
                ResultSet rs2 = stmt.executeQuery(SQL2);  
                rs2.first();
                Recommendations[i][0]=rs2.getString("series_name");
                Recommendations[i][1]=rs2.getString("series_year_began");
                Recommendations[i][2]=rs2.getString("series_year_ended");
                Recommendations[i][3]=rs2.getString("publisher_name");
                Recommendations[i][4]=rs2.getString("series_format");
                Recommendations[i][5]=rs2.getString("series_issue_count");
                Recommendations[i][6]=rs2.getString("series_notes");
                Recommendations[i][7]=rs2.getString("story_synopsis");
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception: "+ e.toString());
        } catch (ClassNotFoundException cE) {
            System.out.println("Class Not Found Exception: "+ cE.toString());
        } 
    }
    
    
    //The following functions are small functions that get used multiple times for each recommendation
    private String[] AddArrays(String a[],String b[]){
        String[] returnArray = new String[a.length + b.length];
        System.arraycopy(a, 0, returnArray, 0, a.length);
        System.arraycopy(b, 0, returnArray, a.length, b.length);
        return returnArray;
    }
	
	private static Boolean RecArrayCheckerString(int a,String b[],float c[],String d) {
        for (int i = 0; i < a; i++) {
            if (b[i].equals(d)){
                c[i]++;
                return false;
            }
        }
        return true;
    }
    
    private static Boolean RecArrayCheckerInt(int a,int b[],int c[],int d) {
        for (int i = 0; i < a; i++) {
            if (b[i]==(d)){
                c[i]++;
                return false;
            }
        }
        return true;
    }
    
    private static Boolean TestForDudString(String a) {
        if (a.equals("")){
            return true;
        }
        else if (a.equals(" ")){
            return true;
        }
        else if (a.contains("?")){
            return true;
        }
        else if (a.contains(")") && !a.contains("(")){
            return true;
        }
        else if (a.isEmpty()){
            return true;
        }
        else if (ContainsNumeric(a)){
            return true;
        }
        else {
            return false;
        }
    }
    
    private static boolean ContainsNumeric(String str){
        return str.matches("-?\\d+(.\\d+)?");
    }
    
    private static Object expandArray(Object a,int b) {
        Class cl = a.getClass();
        if (!cl.isArray()) {
            return null;
        }
        else{
            int length = Array.getLength(a);
            int newLength = length + b; // adding b amount onto the array
            Class componentType = a.getClass().getComponentType();
            Object newArray = Array.newInstance(componentType, newLength);
            System.arraycopy(a, 0, newArray, 0, length);
            return newArray;
        }
    }
    
    
    //final function for testing feedback only, it prints out all the variables gathered in return arrays
    private void ConsoleDisplayForTesting(){
        System.out.println("\nReturns 1:");
        for (int i = 0; i < ReturnNum1; i++) {
            int NumResults=0;
            for (int j = 0; j < InputNum; j++) {
                float copyArray[] = Arrays.copyOf(Return1ArrayWeight[j][i], Return1ArrayIndex[j][i]);
                if (copyArray.length>0){
                    Arrays.sort(copyArray);
                    int TopWeightPoint=Return1ArrayIndex[j][i]-20;
                    if (TopWeightPoint<0){
                        TopWeightPoint=0;
                    }
                    System.out.println(TopWeightPoint+" => "+copyArray[TopWeightPoint]);
                    for (int k = 0; k < Return1ArrayIndex[j][i]; k++) {
                        if (Return1ArrayWeight[j][i][k]>=copyArray[TopWeightPoint]) {
                            System.out.println("Num: "+i+" - "+Return1Array[j][i][k]+" @"+Return1ArrayWeight[j][i][k]);
                            NumResults++;
                        }
                    }
                }
            }
            System.out.println("-----------------------"+NumResults);
        }
        System.out.println("\nReturns 2:");
        for (int i = 0; i < ReturnNum2; i++) {
            for (int j = 0; j < InputNum; j++) {
                for (int k = 0; k < Return2ArrayIndex[j][i]; k++) {
                    System.out.println("Num: "+k+" - "+Return2Array[j][i][k]+" @"+Return2ArrayWeight[j][i][k]);
                }
            }
            System.out.println("-----------------------");
        }
    }
}