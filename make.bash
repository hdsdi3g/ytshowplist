#!/bin/bash

mvn package appassembler:assemble

ln -s $(realpath target/appassembler/bin/ytshowplist) exec

cp -f clientid.json target/appassembler/config/
cp -f setup.properties target/appassembler/config/
cp -rf credentials target/appassembler/config/

