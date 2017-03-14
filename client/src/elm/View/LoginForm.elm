module View.LoginForm exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import Model
import Messages exposing (Msg(..))
import Html.Events exposing (onClick, onInput)


view : Model.Model -> Html Msg
view model =
    case model.auth of
        Model.LoggedOut ->
            div []
                [ div
                    [ class "h1" ]
                    [ text ("Hello, javaBin, You need to log in") ]
                , input
                    [ onInput Messages.UpdateUsername
                    , type_ "text"
                    , name "username"
                    , placeholder "Username"
                    ]
                    []
                , input
                    [ onInput Messages.UpdatePassword
                    , type_ "password"
                    , name "password"
                    , placeholder "Password"
                    ]
                    []
                , button [ onClick LogIn ] [ text "Login" ]
                , case model.loginErr of
                    Nothing ->
                        text ""

                    Just err ->
                        text err
                ]

        Model.LoggedIn token ->
            div
                [ class "h1" ]
                [ text ("Welcome") ]
