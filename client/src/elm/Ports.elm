port module Ports exposing (..)


port login : String -> Cmd msg


port fetchToken : () -> Cmd msg


port fetchedToken : (Maybe String -> msg) -> Sub msg


port logout : () -> Cmd msg


port loggedOut : (() -> msg) -> Sub msg
