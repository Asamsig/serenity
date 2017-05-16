module Model exposing (Model, Auth(..), UserInfo, LoginFormData, init, initModel)

import Ports


type Auth
    = LoggedOut LoginFormData
    | LoggedIn String


type alias LoginFormData =
    { username : String
    , password : String
    , loginErr : Maybe String
    }


type alias UserInfo =
    { userId : String
    , firstName : String
    , lastName : String
    , mainEmail : String
    }


type alias Model =
    { auth : Auth
    , userInfo : Maybe UserInfo
    }


initModel : Model
initModel =
    { auth = LoggedOut (LoginFormData "" "" Nothing)
    , userInfo = Nothing
    }


init : ( Model, Cmd msg )
init =
    ( initModel
    , Ports.fetchToken ()
    )
