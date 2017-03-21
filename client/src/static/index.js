require('./styles/main.scss');
var $ = jQuery = require('../../node_modules/jquery/dist/jquery.js');           // <--- remove if jQuery not needed
require('../../node_modules/bootstrap-sass/assets/javascripts/bootstrap.js');   // <--- remove if Bootstrap's JS not needed

(function () {
    var Elm = require('../elm/Main');
    var app = Elm.Main.embed(document.getElementById('main'));

    app.ports.login.subscribe(function (token) {
        localStorage.setItem("auth-token", token);
    });

    app.ports.getToken.subscribe(function () {
        app.ports.authToken.send(localStorage.getItem("auth-token"));
    });

    app.ports.logout.subscribe(function () {
        localStorage.removeItem("auth-token");
        app.ports.loggedOut.send(null);
    });

})();

