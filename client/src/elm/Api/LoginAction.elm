module Api.LoginAction exposing (loginAction)

import Http
import Json.Decode as Decode
import Json.Encode as Encode
import Ports
import Messages exposing (Msg(LoggedIn))


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
