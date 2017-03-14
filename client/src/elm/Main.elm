module Main exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import View.LoginForm
import Model exposing (Model, Auth(..))
import Messages exposing (Msg(..))


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



-- UPDATE


update : Msg -> Model -> ( Model, Cmd msg )
update msg model =
    case msg of
        NoOp ->
            ( model, Cmd.none )

        LogIn ->
            ( { model | auth = LoggedIn "my token" }, Cmd.none )

        UpdateUsername usr ->
            { model | username = usr } ! []

        UpdatePassword pwd ->
            { model | password = pwd } ! []



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
                    , p [] [ text ("javaBin membership frontend") ]
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
