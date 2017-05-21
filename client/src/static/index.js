require('./styles/main.scss');

(function () {
    var Elm = require('../elm/Main');
    var app = Elm.Main.embed(document.getElementById('main'));

    app.ports.login.subscribe(function (token) {
        localStorage.setItem("auth-token", token);
    });

    app.ports.fetchToken.subscribe(function () {
        app.ports.fetchedToken.send(localStorage.getItem("auth-token"));
    });

    app.ports.logout.subscribe(function () {
        localStorage.removeItem("auth-token");
        app.ports.loggedOut.send(null);
    });

})();

