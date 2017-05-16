module Api.UserInfoAction exposing (userInfoAction)

import Http
import Json.Decode as Decode
import Json.Encode as Encode
import Messages exposing (Msg(UserInfo))
import Model


postWithToken : String -> Http.Body -> String -> Decode.Decoder a -> Http.Request a
postWithToken url body token decoder =
    Http.request
        { method = "POST"
        , headers = [ Http.header "X-Auth-Token" token ]
        , url = url
        , body = body
        , expect = Http.expectJson decoder
        , timeout = Nothing
        , withCredentials = False
        }


graphQlQuery : (Result Http.Error a -> Msg) -> String -> String -> Decode.Decoder a -> Cmd Msg
graphQlQuery toMsg token query decode =
    Http.send
        toMsg
        (postWithToken
            "api/graphql"
            (Http.jsonBody
                (Encode.object
                    [ ( "query"
                      , Encode.string (query)
                      )
                    ]
                )
            )
            token
            (Decode.field "data" decode)
        )


userInfoAction : String -> Cmd Msg
userInfoAction token =
    graphQlQuery
        UserInfo
        token
        (""
            ++ "query user {\n"
            ++ "     user {\n"
            ++ "         userId\n"
            ++ "         firstName\n"
            ++ "         lastName\n"
            ++ "         mainEmail {\n"
            ++ "             address\n"
            ++ "             validated\n"
            ++ "         }\n"
            ++ "         emails {\n"
            ++ "             address\n"
            ++ "             validated\n"
            ++ "         }\n"
            ++ "         createdDate\n"
            ++ "         address\n"
            ++ "         roles\n"
            ++ "         memberships {\n"
            ++ "             issuer\n"
            ++ "             from\n"
            ++ "             to\n"
            ++ "         }\n"
            ++ "     }\n"
            ++ " }"
        )
        (Decode.map4 Model.UserInfo
            (Decode.at [ "user", "userId" ] Decode.string)
            (Decode.at [ "user", "firstName" ] Decode.string)
            (Decode.at [ "user", "lastName" ] Decode.string)
            (Decode.at [ "user", "mainEmail", "address" ] Decode.string)
        )
