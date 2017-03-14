module Messages exposing (Msg(..))

import Http


type Msg
    = NoOp
    | LogIn
    | LoggedIn (Result Http.Error String)
    | UpdateUsername String
    | UpdatePassword String
