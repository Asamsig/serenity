#!/usr/bin/env bash

mv ./node_modules/.bin/elm-make ./node_modules/.bin/elm-make-origin
printf "#\041/bin/bash\n\necho \"Running elm-make with sysconfcpus -n 2\"\n\n$TRAVIS_BUILD_DIR/sysconfcpus/bin/sysconfcpus -n 2 elm-make-origin \"\$@\"" > ./node_modules/.bin/elm-make
chmod +x ./node_modules/.bin/elm-make
