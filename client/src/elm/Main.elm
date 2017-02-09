module Main exposing (..)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing ( onClick )

-- component import example
import Components.Hello exposing ( hello )


-- APP
main : Program Never Int Msg
main =
  Html.beginnerProgram { model = model, view = view, update = update }


-- MODEL
type alias Model = Int

model : number
model = 0


-- UPDATE
type Msg = NoOp | Increment

update : Msg -> Model -> Model
update msg model =
  case msg of
    NoOp -> model
    Increment -> model + 1


-- VIEW
view : Model -> Html Msg
view model =
  div [ class "container", style [("margin-top", "30px"), ( "text-align", "center" )] ][
    div [ class "row" ][
      div [ class "col-xs-12" ][
        div [ class "jumbotron" ][
          hello model
          , p [] [ text ( "javaBin membership frontend" ) ]
          , button [ class "btn btn-primary btn-lg", onClick Increment ] [
            span[ class "glyphicon glyphicon-star" ][]
            , span[][ text "FTW!" ]
          ]
        ]
      ]
    ]
  ]


-- CSS STYLES
styles : { img : List ( String, String ) }
styles =
  {
    img =
      [ ( "width", "33%" )
      , ( "border", "4px solid #337AB7")
      ]
  }
