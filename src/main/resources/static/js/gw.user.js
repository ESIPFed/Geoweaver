/**
 * Manage users
 */

GW.user = {

    login_frame: null,

    signup_frame: null,

    forget_password_frame: null,

    profile_frame: null,

    current_username: null,

    current_userid: null,

    /**
     * Initialization
     */
    init: function(){


    },

    /**
     * Pop up the login dialog
     */
    logindialog : function(){

        GW.user.closeOtherFrames();

        var content = "<div class=\"modal-body\" style=\"font-size: 12px;\" id=\"login_dialog_body\">"+
        
        "<div class=\"row\">"+
        
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="username" class=\"input-lg\" name="username" placeholder="username">'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       <input type="password" id="password" class=\"input-lg\" name="password" placeholder="password">'+
        "   </div>"+
        "</div>"+

        "<div class=\"row\">"+
        "   <div class=\"col-md-12\" >"+
        "       <button onclick=\"GW.user.login()\" class=\"btn btn-lg btn-primary\" >Sign In</button>"+
        "       <button onclick=\"GW.user.signupdialog()\"  class=\"btn btn-lg btn-primary\">Sign Up</button>"+
        '   </div>'+
        "</div>"+

        "<div class=\"row\">"+
        '   <div class=\"col-md-12\">'+
        
        "       <button onclick=\"GW.user.forgetpassdialog()\"  class=\"btn btn-lg btn-primary\">Forgot Password?</button>"+

        "   </div>"+
        "</div>"+
        
        
        "</div>";
        
        this.login_frame = GW.process.createJSFrameDialog(300, 400, content, "Login")

    },

    login: function(){

        if(this.precheck()){

            //password need to be encrypted first

            $.ajax({

                url: "../user/login",
				
                method: "POST",

                headers: { 
                    'Accept': 'application/json',
                    'Content-Type': 'application/json' 
                },
            
                data: "{ \"username\": \""+$("#username").val()+"\", \"password\": \""+$("#password").val()+"\"}"

            }).done(function(msg){

                // msg = $.parseJSON(msg);

                if(msg.status=="success"){

                    console.log("Logged in successfully.");

                    $("#login_dialog_body").html('<div class="row"><div class="col-md-12"><h3>You have logged in successfully!</h3><p>This window will automatically close in 3 seconds..</p></div></div>');

                    $("#toolbar-loginout-a").html("Logout");

                    $("#toolbar-loginout-a").attr("href", "javascript:GW.user.logout()"); 

                    $("#toolbar-profile").html(" "+ msg.username);

                    document.getElementById("toolbar-profile-a").style.visibility = "visible"; 

                    GW.user.current_username = msg.username;

                    GW.user.current_userid = msg.id;

                    // $("#toolbar-login").html($("#username").val() + " " + Logout);

                    setTimeout(function(){
                        GW.user.closeOtherFrames();
                    }, 3000);

                }else{

                    console.log("Login failed");

                }

            }).fail(function(jxr, status){

                console.error("Sign In Failed");

            });

        }

    },


    precheck: function(type){

        var isvalid = true;

        if(type=="reset"){

            if(!$("#email").val()){

                isvalid = false;

            }

        }else{

            if(!$("#username").val()){

                isvalid = false;
    
            }else if(!$("#password").val()){
    
                isvalid = false;
    
            }

        }


        if(!isvalid){

            alert("Either usename or password is missing. ");

        }

        return isvalid;

    },

    /**
     * Send login request
     */
    
    showProfileContent: function(message){

        GW.user.closeOtherFrames();

        var content = "<div class=\"modal-body\" style=\"font-size: 12px;\" id=\"profile_dialog_body\">"+
        
        "<div class=\"row\">"+
        
        "   <div class=\"col-md-12\">"+
        '       User Name: <input type="text" id="username" class=\"input-lg\" name="username" value="'+message.username+'" disabled="disabled"  placeholder="username">'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       Email: <input type="text" id="email" class=\"input-lg\" name="email" value="'+message.email+'" disabled="disabled" placeholder="email">'+
        "   </div>"+
        "</div>"+


        "<div class=\"row\">"+
        '   <div class=\"col-md-12\">'+
        
        "       <button onclick=\"GW.user.closeOtherFrames()\"  class=\"btn btn-lg btn-primary\">Ok</button>"+

        "   </div>"+
        "</div>"+
        
        
        "</div>";
        
        this.profile_frame = GW.process.createJSFrameDialog(300, 400, content, "Profile")

    },

    profiledialog: function(){

        $.ajax({

            url: "../user/profile",
				
            method: "POST",

            headers: { 
                'Accept': 'application/json',
                'Content-Type': 'application/json' 
            },
        
            data: "{ \"id\": \""+GW.user.current_userid+"\"}"

        }).done(function(msg){

            if(msg.status=="success"){

                GW.user.showProfileContent(msg);

            }else{

                console.log("Fail to retrieve user profile");

            }

        }).fail(function(jxr, status){

            console.log("Fail to retrieve user profile " + jxr.message);

        });


    },

    closeOtherFrames: function(){

        try{
        
            if(GW.user.login_frame!=null) GW.user.login_frame.closeFrame();

            if(GW.user.signup_frame!=null) GW.user.signup_frame.closeFrame();

            if(GW.user.forget_password_frame!=null) GW.user.forget_password_frame.closeFrame();

            if(GW.user.profile_frame!=null) GW.user.profile_frame.closeFrame();

            GW.user.login_frame = GW.user.signup_frame = GW.user.forget_password_frame = GW.user.profile_frame = null;


        }catch(error){
            console.error(error);
        }
        

    },

    logout: function(){

        var r = confirm("Do you want to logout?");
        if (r == true) {
            $.ajax({

                url: "../user/logout",
                    
                method: "POST",
    
                headers: { 
                    'Accept': 'application/json',
                    'Content-Type': 'application/json' 
                },
            
                data: "{ \"id\": \""+ GW.user.current_userid + "\"}"
    
            }).done(function(msg){
    
                if(msg.status=="success"){
    
    
                    document.getElementById("toolbar-profile-a").style.visibility = "hidden"; 
    
                    $("#toolbar-loginout-a").html("Login");
    
                    $("#toolbar-loginout-a").attr("href", "javascript:GW.user.logindialog()"); 
    
                    GW.user.current_username = null;
    
                    GW.user.current_userid = null;
    
                }else{
    
                    console.log("Fail to logout");
    
                }
    
            }).fail(function(jxr, status){
    
                console.log("Fail to logout " + jxr.message);
    
            });
        }
        

    },

    signupdialog: function(){

        console.log("Enter sign up dialog..");

        GW.user.closeOtherFrames();

        var content = "<div class=\"modal-body\" style=\"font-size: 12px;\">"+
        
        "<div class=\"row\">"+
        
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="username" class=\"input-lg\" name="username" placeholder="username"/>'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       <input type="password" id="password" class=\"input-lg\" name="password" placeholder="new password"/>'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       <input type="password" id="rpassword" class=\"input-lg\" name="rpassword" placeholder="re-enter password"/>'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="email" class=\"input-lg\" name="email" placeholder="email"/>'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       Do you agree our <a href=\"\">User Terms</a>: <input class="form-check-input" type="radio" name="term_agree" id="agree_yes"> yes <input class="form-check-input" type="radio" name="term_agree" id="agree_no"> no '+
        "   </div>"+
        "</div>"+

        "<p class=\"text-danger\" id=\"server_response_msg\"></p>"+

        "<div class=\"row\">"+
        "   <div class=\"col-md-12\" >"+
        "       <button onclick=\"GW.user.register()\"  class=\"btn btn-lg btn-primary\">Register</button>"+
        "       <button onclick=\"GW.user.closeOtherFrames()\" class=\"btn btn-lg btn-primary\">Cancel</button>"+
        '   </div>'+
        "</div>"+

        "<div class=\"row\">"+
        '   <div class=\"col-md-12\">'+
        
        "       <button onclick=\"GW.user.forgetpassdialog()\" class=\"btn btn-lg btn-primary\">Forgot Password?</button>"+

        "   </div>"+
        "</div>"+
        
        
        "</div>";
        
        this.signup_frame = GW.process.createJSFrameDialog(400, 400, content, "Registration")

    },

    register: function(){

        if(this.precheck()){

            $.ajax({

                url: "../user/register",
				
                method: "POST",

                headers: { 
                    'Accept': 'application/json',
                    'Content-Type': 'application/json' 
                },
            
                data: "{"+

                "    \"username\": \""+$("#username").val()+"\","+
                "    \"password\": \""+$("#password").val()+"\","+
                "    \"email\": \""+$("#email").val()+"\""+

                "}"

            }).done(function(msg){
    
                // msg = $.parseJSON(msg);

                if(msg.status == "success"){

                    console.log("Registration success");

                    GW.user.logindialog();

                }else{

                    console.log("Registration failed.");

                    $("#server_response_msg").html(msg.message);

                }
    
            }).fail(function(jxr, status){
    
                console.log("Registration failed.");

                $("#server_response_msg").html(jxr.message);

            });

        }

    },

    resetPassword: function(){

        if(this.precheck("reset")){

            $.ajax({

                
                url: "../user/forgetpassword",
				
                method: "POST",

                headers: { 
                    'Accept': 'application/json',
                    'Content-Type': 'application/json' 
                },
            
                data: "{"+

                "    \"username\": \""+$("#username").val()+"\","+
                "    \"password\": \""+$("#password").val()+"\","+
                "    \"email\": \""+$("#email").val()+"\""+

                "}"


            }).done(function(msg){
    
                // msg = $.parseJSON(msg);

                if(msg.status == "success"){

                    console.log("Reset success");

                    $("#reset-password-form").html("<h3>A password reset email has been sent to you!</h3> <p>Please check your email to set new passwords.</p>" +
                    "<p>Didn't receive an email? Please try <a href=\"javascript:GW.user.forgetpassdialog()\">resend</a>.</p>");

                }else{

                    console.log("Reset failed.");

                    $("#reset-password-form").html("<h3>Attempt failed</h3>" +
                    "<p>Please try again: <a href=\"javascript:GW.user.forgetpassdialog()\">resend</a>.</p>");

                }
    
            }).fail(function(jxr, status){
    
                console.log("Reset failed.");

                $("#reset-password-form").html("<h2>Attempt failed</h2>" +
                    "<p>Please try again: <a href=\"javascript:GW.user.forgetpassdialog()\">resend</a>.</p>");

            });

        }

    },

    forgetpassdialog: function(){

        GW.user.closeOtherFrames();

        var content = "<div class=\"modal-body\" style=\"font-size: 12px;\" id=\"reset-password-form\">"+
        
        "   <div class=\"row\">"+
        
        "      <div class=\"col-md-12\">"+
        '          <input type="text" id="email"  class=\"input-lg\" name="email" placeholder="email address">'+
        "      </div>"+
        "   </div>"+

        "   <div class=\"row\">"+
        "      <div class=\"col-md-12\" >"+
        "          <button onclick=\"GW.user.resetPassword()\"  class=\"btn btn-lg btn-primary\">Send</button>"+
        "          <button onclick=\"GW.user.closeOtherFrames()\"  class=\"btn btn-lg btn-primary\">Cancel</button>"+
        '      </div>'+
        "   </div>"+
        
        "</div>";
        
        this.forget_password_frame = GW.process.createJSFrameDialog(400, 400, content, "Forget Password")

    },


}
