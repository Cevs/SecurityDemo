$(document).ready(function () {
    var serverContext = "http://localhost:8080";
    $("#buttonResendToken").click(function (event) {
        var token = $("#token").attr("value");
        $.get(serverContext+"/user/resendRegistrationToken?token="+token, function(data){
            window.location.replace(serverContext+"/login?message="+data.message);
        })
            .fail(function (data) {
                console.log(data);
                console.log(data.responseJSON);
                if(data.responseJSON.error.indexOf("MailError")>-1){
                    window.location.replace(serverContext+"emailError.html");
                }
                else{
                    window.location.replace(serverContext+"login?message="+data.responseJSON.message);
                }
            })

    });

});






