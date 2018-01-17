var error_fname;
var error_lname;
var error_email;
var error_email_exist;
var error_password;
var error_repassword;
var  serverContext = "https://localhost:8443";

$(document).ready(function () {

    $("#fname_error_message").hide();
    $("#lname_error_message").hide();
    $("#email_error_message").hide();
    $("#password_error_message").hide();
    $("#repassword_error_message").hide();

    $("#registration_fname").keyup(function () {
        check_fname();
    });

    $("#registration_lname").keyup(function () {
        check_lname();
    });

    $("#registration_password").keyup(function () {
        check_password();
    });
    $("#registration_repassword").keyup(function () {
        check_repassword();
    });

    $("#registration_fname").focusout(function () {
        check_fname();
    });

    $("#registration_lname").focusout(function () {
        check_lname();
    });
    $("#registration_email").focusout(function () {
        check_email();
    });
    $("#registration_password").focusout(function () {
        check_password();
    });
    $("#registration_repassword").focusout(function () {
        check_repassword();
    });

    $(".register-form").submit(function (event) {

        error_fname = false;
        error_lname = false;
        error_email = false;
        error_email_exist = false;
        error_password = false;
        error_repassword = false;

        check_fname();
        check_lname();
        check_email();
        check_password();
        check_repassword();
        if(error_fname === false && error_lname === false && error_email === false
            && error_email_exist === false && error_password === false && error_repassword === false){
            console.log("fname:"+error_fname);
            console.log("lname:"+error_lname);
            console.log("email:"+error_email);
            console.log("exist:"+error_email_exist);
            console.log("passwrod:"+error_password);
            console.log("repassword:"+error_repassword);
            console.log("OK");
            register(event);
        }else{
            //Prevent registraton
            console.log("fname:"+error_fname);
            console.log("lname:"+error_lname);
            console.log("email:"+error_email);
            console.log("exist:"+error_email_exist);
            console.log("passwrod:"+error_password);
            console.log("repassword:"+error_repassword);
            console.log("not OK");
            event.preventDefault();
        }
    });

});

function register(event){

    event.preventDefault();
    var formData = $('form').serialize();
    $.post(serverContext+"/user/registration",formData, function(data){
        if(data.message === "success"){
            window.location.replace(serverContext+"/successRegister.html");
        }

    })
        .fail(function (data) {
            console.log(data);
            if(data.responseJSON.error.indexOf("MailError")>-1){
                window.location.replace(serverContext+"/emailError.html");
            }
            else if (data.responseJSON.error == "InvalidReCaptcha"){
                grecaptcha.reset();
            }
            else if(data.responseJSON.error.indexOf("InternalError")>-1){
                window.location.href = serverContext+"/login.html?message="+data.responseJSON.message;
            }
            else if(data.responseJSON.error == "UserAlreadyExist"){
                $("#email_error_message").show().html(data.responseJSON.message);
            }
            else{
                var errors = $.parseJSON(data.responseJSON.message);
                $.each( errors, function( index,item ){
                    $("#"+item.field+"Error").show().html(item.defaultMessage);
                });
                errors = $.parseJSON(data.responseJSON.error);
                $.each( errors, function( index,item ){
                    $("#globalError").show().append(item.defaultMessage+"<br>");
                });
            }
        });

}

function check_fname(){
    pattern = /^[a-zA-Z]*$/; //We want only letters
    fname = $("#registration_fname").val();
    if(pattern.test(fname) && fname !== ''){
        $("#fname_error_message").hide();
        $("#registration_fname").css("border-bottom","2px solid #34f358");
    }else{
        $("#fname_error_message").html("Should cointain only charachters!");
        $("#fname_error_message").show();
        $("#registration_fname").css("border-bottom","2px solid #F90A0A");
        error_fname = true;
    }
}

function check_lname(){
    pattern = /^[a-zA-Z]*$/;
    lname = $("#registration_lname").val();
    if(pattern.test(lname) && lname !== ''){
        $("#lname_error_message").hide();
        $("#registration_lname").css("border-bottom","2px solid #34f358");
    }else{
        $("#lname_error_message").html("Should cointain only charachters!");
        $("#lname_error_message").show();
        $("#registration_lname").css("border-bottom","2px solid #F90A0A");
        error_lname = true;
    }
}

function check_email() {
    pattern = /[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?/;
    email = $("#registration_email").val();
    if (pattern.test(email) && email !== '') {
        $("#email_error_message").hide();
        $("#registration_email").css("border-bottom", "2px solid #34F458");

    } else {
        $("#email_error_message").html("Invalid email");
        $("#email_error_message").show();
        $("#registration_email").css("border-bottom", "2px solid #F90A0A");
        error_email = true;
    }

    if(!error_email){
        check_if_exists();
    }


}

function check_if_exists(){
    $.ajax({
        async: false,
        type: "POST",
        url: serverContext+"/user/exist",
        data: {
            email: email
        },
        success:function (data) {
            if(data === true){
                error_email_exist = true;
                $("#email_error_message").html("Email already exists");
                $("#email_error_message").show();
                $("#registration_email").css("border-bottom", "2px solid #F90A0A");
            }else{
                $("#email_error_message").hide();
                $("#registration_email").css("border-bottom", "2px solid #34F458");
            }
        },
    });

}


function check_password(){
    pattern = /((?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\W]).{8,64})/;
    password = $("#registration_password").val();
    if(!pattern.test(password)){
        $("#password_error_message").html("Password too weak. (minimum 8 characters, 1 Uppercase, 1 number and 1 special character");
        $("#password_error_message").show();
        $("#registration_password").css("border-bottom","2px solid #F90A0A");
        error_password = true;
    }else{
        $("#password_error_message").hide();
        $("#registration_password").css("border-bottom","2px solid #34F458");
    }
}

function check_repassword(){
    var password = $("#registration_password").val();
    var repassword = $("#registration_repassword").val();
    if(password !== repassword || password === ''){
        $("#repassword_error_message").html("Passwords do not match");
        $("#repassword_error_message").show();
        $("#registration_repassword").css("border-bottom","2px solid #F90A0A");
        error_repassword = true;
    }else
    {
        $("#repassword_error_message").hide();
        $("#registration_repassword").css("border-bottom","2px solid #34F458");
    }
}

