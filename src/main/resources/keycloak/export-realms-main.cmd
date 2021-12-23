@echo off

For /f %%i in ('docker ps -f "ancestor=quay.io/keycloak/keycloak:15.0.2" -q') do set "CONTAINERID=%%i"

rem # copy the script into the docker container
docker cp export-realm.sh %CONTAINERID%:/tmp/export-realm.sh

rem # execute the export
docker exec -it %CONTAINERID% bash /tmp/export-realm.sh -r comact -o /tmp/comact-realm.json

rem # retrieve the export file
docker cp %CONTAINERID%:/tmp/comact-realm.json comact-realm.json