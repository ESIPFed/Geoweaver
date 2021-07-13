/**
 * Manage users
 */

GW.user = {

    /**
     * Initialization
     */
    init: function(){


    },

    /**
     * Pop up the login dialog
     */
    login_dialog: function(){

        var content = "<div class=\"modal-body\" style=\"font-size: 12px;\">"+
        
        "<div class=\"row\">"+
        
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="login"  name="login" placeholder="login">'+
        "   </div>"+
        "   <div class=\"col-md-12\">"+
        '       <input type="text" id="password" name="password" placeholder="password">'+
        "   </div>"+
        "</div>"+

        "<div class=\"row\">"+
        "   <div class=\"col-md-12\" >"+
        "       <button click=\"GW.user.login()\">Sign In</button>"+
        "       <button click=\"GW.user.signupdialog()\">Sign Up</button>"+
        '   </div>'+
        "</div>"+

        "<div class=\"row\">"+
        '   <div class=\"col-md-12\">'+
        
        "       <button click=\"GW.user.forgetpassdialog()\">Forgot Password?</button>"+

        "   </div>"+
        "</div>"+
        
        
        "</div>";
        
        var frame = GW.process.createJSFrameDialog(300, 300, content, "Login")

    },

    /**
     * Send login request
     */
    login: function(){



    },

    signupdialog: function(){

        

    },

    /**
     * Send logout request
     */
    logout: function(){



    }


}
