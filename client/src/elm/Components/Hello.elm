module Components.Hello exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import String
import Model exposing (..)


hello : Model -> Html a
hello model =
    case model.auth of
        LoggedOut ->
            div
                [ class "h1" ]
                [ text ("Hello, javaBin, You need to log in") ]

        LoggedIn token ->
            div
                [ class "h1" ]
                [ text ("Hello, javaBin, You're logged in: Token " ++ token) ]
