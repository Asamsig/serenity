module Model exposing (Model, Auth(..), LoginFormData, init, initAuthModel)

import Ports


type Auth
    = LoggedOut LoginFormData
    | LoggedIn String


type alias LoginFormData =
    { username : String
    , password : String
    , loginErr : Maybe String
    }


type alias Model =
    { auth : Auth
    }


initAuthModel : Auth
initAuthModel =
    (LoggedOut (LoginFormData "" "" Nothing))


init : ( Model, Cmd msg )
init =
    ( Model initAuthModel
    , Ports.getToken ()
    )
