<%@page import="RecommenderPackages.RecEngine"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Comics Book Recommender - Search by Writer & Artist</title>
        <meta name="description" content="This is the website that gives recommendations for comic books"/>
        <meta name="keywords" content="recommendation, series, Characters, Writers, Artists, comics, comic book"/>
        <meta http-equiv="Content-Language" content="en"/>
        
        <link type="text/css" rel="stylesheet" href="./resources/style.css"/>
        <link type="text/css" rel="stylesheet" media="all" href="./resources/jquery-ui-1.9.2.custom.css" >
        
        <script type="text/javascript" src="./resources/jquery-1.4.4.min.js"></script> 
        <script type="text/javascript" src="./resources/jquery-ui-1.8.9.custom.min.js"></script> 
        <script type="text/javascript" src="./resources/ArtistNames.js"></script> 
        <script type="text/javascript" src="./resources/site.js"></script>
        
        <script>	
            function AddToInputArray(inputPlace){
                var InputChecker=0;
                for (var i=0;i<ArtistNames.length;i++){
                    if (ArtistNames[i].toLowerCase()==inputPlace.toLowerCase()){
                        InputChecker=1;
                    }
                }
                if (InputChecker>0){
                    InputArray[InputArrayIndex]=inputPlace;
                    InputArrayIndex++;
                    ArrayBoxesSetup();
                    $("#FeedbackParagraph1").html("");
                }
                else{
                    $("#FeedbackParagraph1").html("<p style='text-align:center;'>You have not entered a valid artist/writer's Name, please enter another selection</p>");
                }
                $("#CharacterNamesClientInput").val('');
            }
        </script>
        
        <%
            String SearchNameString="";
            String LikeNameString="";
            String DislikeNameString="";
            int SlidervalueString=4;
            if(request.getParameter("SendingString1")!= null){
                SearchNameString = request.getParameter("SendingString1");
                LikeNameString = request.getParameter("SendingString2");
                DislikeNameString = request.getParameter("SendingString3");
                SlidervalueString = Integer.parseInt(request.getParameter("points"));
            }                                         
        %>
    </head>
    <body lang="en">
        <div id="backarea">
            <div id="bodyborder">
                <div id="sitebody">				
                    <div id="menu">
                        <ul id="menulist1">
                            <li id="banner"><a href="./index.html"><p class="invisible">Banner and Home Button</p></a></li>
                        </ul>
                        <ul id="menulist2">								
                            <li id="Series"><a href="./Series.jsp"><p class="invisible">By Series Button</p></a></li>
                            <li id="Artists"><a href="./Artists.jsp"><p class="invisible">By Artists Button</p></a></li>
                            <li id="Characters"><a href="./Characters.jsp"><p class="invisible">By Characters Button</p></a></li>
                        </ul>
                    </div>
                    <hr style="color:#002065;background-color:#002065;height: 0.2em;">
                    <form method="post">
                        <div id="sitepage">
                            <h1 class="invisible">Comic Book Recommender</h1>                 
                            <div id="FormInputs">
                                <h2 style="text-align:center;">Search by Writer & Artist</h2>
                                <p><em>Step 1:</em><br>Type in the names of a Writer or Artist you like, then click "Add". Do this for as many writers/artists as you want, the more names you enter the higher the accuracy.</p> 
                                <div class="CenterObject" >
                                    <input style="height:1.8em;width:24em;font-size:1.25em;padding-left:0.625em;float:left;margin:0 0.5em 0 0;" type="text" id="CharacterNamesClientInput" name="SearchNameBox" placeholder="Search Writer / Artist Names" />
                                    <img style="height:2.65em;" id="AddStringButton" src="./resources/images/AddButton.jpg"/>
                                </div>
                                <div id="FeedbackParagraph1" style="color:red;"></div>
                                <div id="SelectedStrings" class="CenterObject" ></div>    
                                <img style="width:6em;" id="ClearButton" class="CenterObject" src="./resources/images/ClearButton.jpg"/>
                                <p><em>Step 2:</em><br>Select how accurate you want the recommendations with the slider. The lower the accuracy the faster the recommendations will be given, the higher the accuracy the slower it will be.</p>
                                <!--This hack is for internet explorer, currently it displays the range input as a text box-->
                                <!--[If IE]> <p>Enter A value between 1 and 7 (1=lowest,7=highest)</p> <![endif]-->
                                <div id="FireFoxDivision"></div>
                                <%
                                    out.println("<input type='hidden' name='SendingString1' value='"+SearchNameString.replace("'","&#39;")+"' id='SendingString1'>");
                                    out.println("<input type='hidden' name='SendingString2' value='"+LikeNameString.replace("'","&#39;")+"' id='SendingString2'>");
                                    out.println("<input type='hidden' name='SendingString3' value='"+DislikeNameString.replace("'","&#39;")+"' id='SendingString3'>");
                                    out.println("<div style='font-size:1.5em;color:#002065;' class='CenterObject'><label>Accuracy: &nbsp;&nbsp;</label>");
                                    out.println("<label>Low&nbsp;</label><input style='width:12.5em;' value='"+SlidervalueString+"' type='range' name='points' min='1' max='7' step='1'>");
                                    out.println("<label>&nbsp;High&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label></div>");
                                %>
                                <p><em>Step 3:</em><br>Click to get Recommendations!</p>
                                <div style='text-align:center;'>
                                    <input style="width:25em;" type="image" src="./resources/images/RecButton.jpg" alt="Submit button" class="SendInputs">
                                </div>
                                <br>
                            </div>
                            <br>  
                            <div id="LoadingDivision" style="display:none;">
                                <br><br><br><br><br><br>
                                <img style="width:7.5em;" class="CenterObject" src="./resources/images/loadingB.gif" alt="loading image" id="HiddenImage"/>
                                <p class="CenterObject">Loading...</p>
                                <br>
                                <p class="CenterObject">This may take 10-20 Seconds (Please to not refresh the page)</p>
                                <br><br><br><br><br><br><br><br><br><br><br><br>
                            </div>
                        </div>
                        <hr id="middlerule" style="color:#002065;background-color:#002065;height: 0.25em;">
                        <div id="sitepage2">
                            <div id="ResultDivision">
                            <% 
                                if(SearchNameString!=""){
                                    //whitebox testing
                                    //out.println("<p>Your SendingString1 is: " + SearchNameString+"</p>");
                                    //out.println("<p>Your LikeNameString is: " + LikeNameString+"</p>");
                                    //out.println("<p>Your DislikeNameString is: " + DislikeNameString+"</p>");
                                    //out.println("<p>Your SlidervalueString is: " + SlidervalueString+"</p>");

                                    String TestArray1[]=SearchNameString.split(";");
                                    String TestArray2[];
                                    String TestArray3[];
                                    if (LikeNameString!=""){
                                        TestArray2=LikeNameString.split(";");
                                    }
                                    else{
                                        TestArray2=new String[0];
                                    }
                                    if (DislikeNameString!=""){
                                        TestArray3=DislikeNameString.split(";");
                                    }
                                    else{
                                        TestArray3=new String[0];
                                    }
                                    int testNumOfRecWanted=30;
                                    int whatTypeOfSerch=1;
                                    int TestSlidervalueString=SlidervalueString;

                                    RecEngine TestInput=new RecEngine();
                                    String ResultArray[][];
                                    ResultArray=TestInput.GetRecommendations(TestArray1,whatTypeOfSerch,testNumOfRecWanted,TestSlidervalueString,TestArray2,TestArray3);

                                    out.println("<h3 style='margin-left:15.5em;'>Results</h3>");
                                    out.println("<p style='padding:0;margin:0;border:0;'>Here Are your recommended series. You can now click the Thumbs Up or Down Buttons depending on your preferance, when you are happy with what you selected, press the 'Get Recommendations' button again. You can also click the 'more info' for more detailed information.</p>");
                                    out.println("<div style='text-align:right;'><input style='width:12.5em;' type='image' src='./resources/images/MoreButton.jpg' alt='Submit button' class='SendInputs'></div>");
                                    
                                    for (int i = 0; i < ResultArray.length; i++) {
                                        if (ResultArray[i][2]==null){
                                            ResultArray[i][2]="Currently Ongoing";
                                        }
                                        out.println("<div class='SeriesWrap'>");
                                        out.println("<div class='SeriesTitle'><label style='display: block; width:37.5em;'>"+(i+1)+") <strong id='RecommendedTitle"+i+"' title='"+"0"+"' >"+ResultArray[i][0]+"</strong></label>");
                                        out.println("<img src='./resources/images/Info.jpg' class='flipbutton' style='height:1.5em;margin-top:0.625em;' name='"+i+"'>");
                                        out.println("<img src='./resources/images/TD1.jpg' id='DisButton"+i+"' class='Dislikebutton' style='float:right;height:2.1875em;margin-right: 0.5em;' name='"+i+"'>");
                                        out.println("<img src='./resources/images/TU1.jpg' id='LikButton"+i+"' class='Likebutton' style='float:right;height:2.1875em;margin-right: 0.5em;' name='"+i+"'></div>");
                                        out.println("<div class='SeriesPanel' id='panel"+i+"' ><p><strong>Published By: </strong>"+ResultArray[i][3]+"</p><p><strong>Story: </strong>"+ResultArray[i][7]+"</p>");
                                        out.println("<p><strong>Year Began: </strong>"+ResultArray[i][1]+"</p><p><strong>Year Ended: </strong>"+ResultArray[i][2]+"</p>");
                                        out.println("<p><strong>Format: </strong>"+ResultArray[i][4].replaceAll(";",", ")+"</p><p><strong>Issue Count: </strong>"+ResultArray[i][5]+"</p>");
                                        out.println("<p><strong>Additional Notes: </strong>"+ResultArray[i][6]+"</p></div>");
                                        out.println("</div>");
                                    }
                                    out.println("<div style='text-align:right;'><input style='width:12.5em;margin-top:0.5em;' type='image' src='./resources/images/MoreButton.jpg' alt='Submit button' class='SendInputs'></div>");
                                }
                                else{
                                    out.println("<br><h3 style='text-align:center'>Top 20 Searches (click to add to selected)</h3>");
                                    out.println("<table style='margin-left:15.625em;' ><tr><td style='width:11.875em;'><ul class='alpha'>");
                                    out.println("<li class='AddListClicked'>Grant Morrison</li>");
                                    out.println("<li class='AddListClicked'>Brian Michael Bendis</li>");
                                    out.println("<li class='AddListClicked'>John Cassaday</li>");
                                    out.println("<li class='AddListClicked'>Jonathan Hickman</li>");
                                    out.println("<li class='AddListClicked'>Ed Brubaker</li>");
                                    out.println("<li class='AddListClicked'>Warren Ellis</li>");
                                    out.println("<li class='AddListClicked'>Jeph Loeb</li>");
                                    out.println("<li class='AddListClicked'>Mark Waid</li>");
                                    out.println("<li class='AddListClicked'>Geoff Johns</li>");
                                    out.println("<li class='AddListClicked'>Brian K. Vaughan</li>");
                                    out.println("</ul></td><td><ul class='alpha'>");
                                    out.println("<li class='AddListClicked'>John Romita Jr.</li>");
                                    out.println("<li class='AddListClicked'>Frank Quitely</li>");
                                    out.println("<li class='AddListClicked'>Rick Remender</li>");
                                    out.println("<li class='AddListClicked'>Eduardo Risso</li>");
                                    out.println("<li class='AddListClicked'>Gabriele Dell'otto</li>");
                                    out.println("<li class='AddListClicked'>Mike Deodato</li>");
                                    out.println("<li class='AddListClicked'>Jim Lee</li>");
                                    out.println("<li class='AddListClicked'>Tim Sale</li>");
                                    out.println("<li class='AddListClicked'>Steve McNiven</li>");
                                    out.println("<li class='AddListClicked'>Dave Johnson</li></ul></td></tr></table>");
                                }
                            %>
                            </div>			
                        </div>
                    </form>
                    <br><br>
                    <div id="footer">
                        <h2 class="invisible">Footer</h2>
                        <p style="color:white;text-align:center;">Here are also some <a href="./links.html" class="whitelink"><strong>USEFUL LINKS</strong></a> to external websites you might be interested in viewing</p>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>