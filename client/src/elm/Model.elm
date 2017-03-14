module Model exposing (Model, Auth(..), init)


type Auth
    = LoggedOut
    | LoggedIn String


type alias Model =
    { auth : Auth
    , username : String
    , password : String
    , loginErr : Maybe String
    }


init : ( Model, Cmd msg )
init =
    ( Model LoggedOut "" "" Nothing, Cmd.none )
