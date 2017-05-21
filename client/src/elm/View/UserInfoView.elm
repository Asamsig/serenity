module View.UserInfoView exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import Model
import Messages exposing (Msg(..))
import Html.Events exposing (onClick, onInput)


userInfoView : Model.Model -> Html Msg
userInfoView model =
    div []
        [ div
            [ class "h1" ]
            [ text "Welcome"
            , button [ onClick Messages.LogOut ] [ text "Log Out" ]
            ]
        , (case model.userInfo of
            Just ui ->
                div []
                    [ div [] [ span [] [ text ("First name: " ++ ui.firstName) ] ]
                    , div [] [ span [] [ text ("Last name: " ++ ui.lastName) ] ]
                    ]

            Nothing ->
                div [] [ text "Loading..." ]
          )
        ]
