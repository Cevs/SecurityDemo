var error_fname;
var error_lname;
var error_email;
var error_email_exist;
var error_password;
var error_repassword;

$(document).ready(function () {

    $("#fname_error_message").hide();
    $("#lname_error_message").hide();
    $("#email_error_message").hide();
    $("#password_error_message").hide();
    $("#repassword_error_message").hide();

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
    serverContext = "http://localhost:8080";
    var formData = $('form').serialize();
    $.post(serverContext+"/user/registration",formData, function(data){
        if(data.message === "success"){
            window.location.replace(serverContext+"/successRegister.html");
        }
        else if(data.message === "failure" ){
            grecaptcha.reset();
        }
        console.log("success");
    })
        .fail(function (data) {
            grecaptcha.reset();
        });

}

function check_fname(){
    var pattern = /^[a-zA-Z]*$/; //We want only letters
    var fname = $("#registration_fname").val();
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
    var pattern = /^[a-zA-Z]*$/;
    var lname = $("#registration_lname").val();
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
    serverContext = "http://localhost:8080";
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
    var password_length = $("#registration_password").val().length;
    if(password_length < 8){
        $("#password_error_message").html("Atleast 8 characters");
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

