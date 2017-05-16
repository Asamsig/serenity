module Messages exposing (Msg(..))

import Model
import Http


type Msg
    = LogIn
    | LoggedIn (Result Http.Error String)
    | LogOut
    | LoggedOut ()
    | UpdateUsername String
    | UpdatePassword String
    | StoredToken (Maybe String)
    | UserInfo (Result Http.Error Model.UserInfo)
