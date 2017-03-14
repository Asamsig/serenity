module Model exposing (Model, Auth(..), init)


type Auth
    = LoggedOut
    | LoggedIn String


type alias Model =
    { auth : Auth }


init : ( Model, Cmd msg )
init =
    ( Model LoggedOut, Cmd.none )
