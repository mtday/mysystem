#!/usr/bin/env bash

cd $(dirname $0)

mvn exec:java -pl :mysystem-shell -Dexec.mainClass="mysystem.shell.run.Shell"
