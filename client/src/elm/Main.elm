module Main exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import View.LoginForm
import Model exposing (Model)
import Messages exposing (Msg(..))
import Ports
import Api.LoginAction exposing (loginAction)
import Api.UserInfoAction exposing (userInfoAction)


-- APP


main : Program Never Model Msg
main =
    Html.program
        { view = view
        , update = update
        , init = Model.init
        , subscriptions = sub
        }


sub : Model -> Sub Msg
sub model =
    Sub.batch
        [ Ports.fetchedToken Messages.StoredToken
        , Ports.loggedOut Messages.LoggedOut
        ]



-- UPDATE


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        LogIn ->
            case model.auth of
                Model.LoggedIn token ->
                    model ! []

                Model.LoggedOut formData ->
                    model ! [ loginAction formData.username formData.password ]

        UpdateUsername usr ->
            case model.auth of
                Model.LoggedIn token ->
                    model ! []

                Model.LoggedOut formData ->
                    { model | auth = Model.LoggedOut { formData | username = usr, loginErr = Nothing } } ! []

        UpdatePassword pwd ->
            case model.auth of
                Model.LoggedIn token ->
                    model ! []

                Model.LoggedOut formData ->
                    { model | auth = Model.LoggedOut { formData | password = pwd, loginErr = Nothing } } ! []

        LoggedIn (Err e) ->
            case model.auth of
                Model.LoggedIn token ->
                    { model | auth = (Model.LoggedOut (Model.LoginFormData "" "" Nothing)) } ! []

                Model.LoggedOut formData ->
                    { model | auth = Model.LoggedOut { formData | loginErr = Just "login_failed" } } ! []

        LoggedIn (Ok token) ->
            { model | auth = Model.LoggedIn token } ! [ Ports.login token, userInfoAction token ]

        LogOut ->
            model ! [ Ports.logout () ]

        LoggedOut () ->
            Model.initModel ! []

        StoredToken mbyToken ->
            case mbyToken of
                Just token ->
                    { model | auth = Model.LoggedIn token } ! [ userInfoAction token ]

                Nothing ->
                    model ! []

        UserInfo (Ok res) ->
            { model | userInfo = Just res } ! []

        UserInfo (Err e) ->
            model ! []



-- VIEW


view : Model -> Html Msg
view model =
    div
        [ style
            [ ( "margin-top", "30px" )
            , ( "text-align", "center" )
            ]
        ]
        [ div []
            [ div []
                [ div []
                    [ View.LoginForm.view model
                    , p [] [ text ("javaBin  membership frontend") ]
                    ]
                ]
            ]
        ]



-- CSS STYLES


styles : { img : List ( String, String ) }
styles =
    { img =
        [ ( "width", "33%" )
        , ( "border", "4px solid #337AB7" )
        ]
    }
