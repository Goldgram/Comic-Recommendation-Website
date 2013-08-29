var NumberOfresults=30;
var InputArray = new Array();
var InputArrayIndex=0;

$(window).load(function (){
    var StartString=$("#SendingString1").val().split(";");
    for (var i=0;i<StartString.length;i++)
    {
        InputArray[InputArrayIndex]=StartString[i];
        InputArrayIndex++;
    }
    ArrayBoxesSetup();

    if ( $.browser.mozilla )
    {
        $("#FireFoxDivision").html("<p>Enter A value between 1 and 7 (1=lowest,7=highest)<p>");
    }

    $(document).ready(function (){
        $(".SendInputs").click(function(){
            UpdateLikesAndDislikes();
            $("#LoadingDivision").show();
            $("#FormInputs").hide();
            $("#middlerule").hide();
            $("#ResultDivision").hide();
            $("#defaultLists").hide();
        });
    });
});

function ArrayBoxesSetup(){
    var tempInputText="";
    $("#SelectedStrings").html("");
    for (var i=0;i<InputArray.length;i++){
        if (InputArray[i]!=""){
            tempInputText+=InputArray[i]+";";
            $("#SelectedStrings").append("<div class='ArrayBoxes'><label><strong>"+InputArray[i]+" </strong></label><img class='DeleteButton' src='./resources/images/circleX2.jpg' name='"+i+"'/></div>");
        }
    }

    $("#SendingString1").val(tempInputText);
    if ($("#SelectedStrings").html()==""){
        $("#ClearButton").hide();
    }
    else{
        $("#ClearButton").show();
    }

    $(document).ready(function (){
        $(".DeleteButton").click(function(){
            InputArray.splice(($(this).attr("name")), 1);
            InputArrayIndex--;
            ArrayBoxesSetup();
            if ($("#SendingString1").val()==""){
                $("#SendingString2").val("");
                $("#SendingString3").val("");
            }
        });

        $("#ClearButton").click(function(){
            InputArray.length = 0;
            InputArrayIndex=0;
            $("#SendingString1").val("");
            $("#SendingString2").val("");
            $("#SendingString3").val("");
            for (var i=0;i<NumberOfresults;i++){
                $("#RecommendedTitle"+i).attr("name","0");  
            }
            ArrayBoxesSetup();
        });
    });
}

function UpdateLikesAndDislikes(){
    var LikeString="";
    var DislikeString="";
    for (var i=0;i<NumberOfresults;i++){
        var tempNum=$("#RecommendedTitle"+i).attr("name");
        if (tempNum=="1"){
            LikeString+=($("#RecommendedTitle"+i).text())+";";
        }
        else if (tempNum=="2"){
            DislikeString+=($("#RecommendedTitle"+i).text())+";";
        }
    }
    $("#SendingString2").val(($("#SendingString2").val())+(LikeString));
    $("#SendingString3").val(($("#SendingString3").val())+(DislikeString));
}

$(document).keypress(function(e) {
    if(e.which == 13) {
        AddToInputArray($("#CharacterNamesClientInput").val());
        event.preventDefault();
    }
});

$(document).ready(function (){
    $("#AddStringButton").click(function(){
        AddToInputArray($("#CharacterNamesClientInput").val());
    });

    $(".AddListClicked").click(function(){
        AddToInputArray($(this).text());
    });

    $(".flipbutton").click(function(){
        $("#panel"+$(this).attr("name")).slideToggle(400);
    });

    $(".Likebutton").click(function(){
        var tester =$("#RecommendedTitle"+($(this).attr("name"))).attr("name");
        if (tester=="1"){
            $("#RecommendedTitle"+($(this).attr("name"))).attr("name","0");
            document.getElementById("LikButton"+($(this).attr("name"))).src = "./resources/images/TU1.jpg";
        }
        else{
            $("#RecommendedTitle"+($(this).attr("name"))).attr("name","1");
            document.getElementById("LikButton"+($(this).attr("name"))).src = "./resources/images/TU2.jpg";
            document.getElementById("DisButton"+($(this).attr("name"))).src = "./resources/images/TD1.jpg";
        }
    });

    $(".Dislikebutton").click(function(){
        var tester =$("#RecommendedTitle"+($(this).attr("name"))).attr("name");
        if (tester=="2"){
            $("#RecommendedTitle"+($(this).attr("name"))).attr("name","0");
            document.getElementById("DisButton"+($(this).attr("name"))).src = "./resources/images/TD1.jpg";
        }
        else{
            $("#RecommendedTitle"+($(this).attr("name"))).attr("name","2");
            document.getElementById("DisButton"+($(this).attr("name"))).src = "./resources/images/TD2.jpg";
            document.getElementById("LikButton"+($(this).attr("name"))).src = "./resources/images/TU1.jpg";
        }
    });

    //buttons for testing whitebox testing
    /*
    $("#testbutton1").click(function(){
        alert("Value: " + $("#SendingString1").val());
    });

    $("#testbutton2").click(function(){
        alert("Value: " + $("#SendingString2").val());
    });

    $("#testbutton3").click(function(){
        alert("Value: " + $("#SendingString3").val());
    });*/
});