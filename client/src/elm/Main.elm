module Main exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick)
import Components.Hello exposing (hello)
import Model exposing (Model)


-- APP


main : Program Never Model Msg
main =
    Html.program
        { view = view
        , update = update
        , init = Model.init
        , subscriptions = sub
        }


model : number
model =
    0


sub : Model -> Sub msg
sub model =
    Sub.none



-- UPDATE


type Msg
    = NoOp
    | Increment


update : Msg -> Model -> ( Model, Cmd msg )
update msg model =
    case msg of
        NoOp ->
            ( model, Cmd.none )

        Increment ->
            ( model, Cmd.none )



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
                    [ hello model
                    , p [] [ text ("javaBin membership frontend") ]
                    , button
                        [ class "btn btn-primary btn-lg"
                        , onClick Increment
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
