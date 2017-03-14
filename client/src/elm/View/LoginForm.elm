module View.LoginForm exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import Model exposing (..)
import Messages exposing (Msg(..))
import Html.Events exposing (onClick, onInput)


view : Model -> Html Msg
view model =
    case model.auth of
        LoggedOut ->
            div []
                [ div
                    [ class "h1" ]
                    [ text ("Hello, javaBin, You need to log in") ]
                , input
                    [ onInput Messages.UpdateUsername
                    , type_ "password"
                    , name "username"
                    , placeholder "Username"
                    ]
                    []
                , input
                    [ onInput Messages.UpdatePassword
                    , type_ "text"
                    , name "password"
                    , placeholder "Password"
                    ]
                    []
                , button [ onClick LogIn ] [ text "Login" ]
                ]

        LoggedIn token ->
            div
                [ class "h1" ]
                [ text ("Hello, javaBin, You're logged in, token: " ++ token) ]
