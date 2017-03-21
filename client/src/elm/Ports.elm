port module Ports exposing (..)


port login : String -> Cmd msg


port getToken : () -> Cmd msg


port authToken : (Maybe String -> msg) -> Sub msg
