module Main exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import View.LoginForm
import Model exposing (Model)
import Messages exposing (Msg(..))
import Http
import Json.Decode as Decode
import Json.Encode as Encode


-- APP


main : Program Never Model Msg
main =
    Html.program
        { view = view
        , update = update
        , init = Model.init
        , subscriptions = sub
        }


sub : Model -> Sub msg
sub model =
    Sub.none


loginAction : String -> String -> Cmd Msg
loginAction usr pwd =
    Http.send
        LoggedIn
        (Http.post
            "api/login"
            (Http.jsonBody
                (Encode.object
                    [ ( "username", Encode.string usr )
                    , ( "password", Encode.string pwd )
                    ]
                )
            )
            (Decode.field "token" Decode.string)
        )



-- UPDATE


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        NoOp ->
            ( model, Cmd.none )

        LogIn ->
            model ! [ loginAction model.username model.password ]

        UpdateUsername usr ->
            { model | username = usr } ! []

        UpdatePassword pwd ->
            { model | password = pwd } ! []

        LoggedIn (Err e) ->
            { model | loginErr = Just "login_failed" } ! []

        LoggedIn (Ok token) ->
            { model
                | auth = Model.LoggedIn token
                , loginErr = Nothing
                , username = ""
                , password = ""
            }
                ! []



-- VIEW


view : Model -> Html Msg
view model =
    div
        [ class "container"
        , style
            [ ( "margin-top", "30px" )
            , ( "text-align", "center" )
            ]
        ]
        [ div [ class "row" ]
            [ div [ class "col-xs-12" ]
                [ div [ class "jumbotron" ]
                    [ View.LoginForm.view model
                    , p [] [ text ("javaBin  membership frontend") ]
                    , button
                        [ class "btn btn-primary btn-lg"
                        , onClick LogIn
                        ]
                        [ span [ class "glyphicon glyphicon-star" ] []
                        , span [] [ text "FTW!" ]
                        ]
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
