module Messages exposing (Msg(..))

import Http


type Msg
    = LogIn
    | LoggedIn (Result Http.Error String)
    | UpdateUsername String
    | UpdatePassword String
    | StoredToken (Maybe String)
