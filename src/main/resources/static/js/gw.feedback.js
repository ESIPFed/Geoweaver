GW.feedback = {

    showDialog: function () {

        var content = `<div style="padding:10px">
            <h3 class="text-center">Have feedback? We'd love to hear it!</h3>

            <p class="text-center"> Let us know what you think of Geoweaver and how we can improve your Geoweaver experience.</p> </br> </br>
            <p class="text-center"> <a class="btn btn-success" role="button" target="_blank" href="https://forms.gle/FhHXFYdPmBdGtsUM8" >Get Started</a>  </p>`;


        GW.process.createJSFrameDialog(460, 320, content, "Feedback");
        
    },
}