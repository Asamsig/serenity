module Messages exposing (Msg(..))


type Msg
    = NoOp
    | LogIn
    | UpdateUsername String
    | UpdatePassword String
