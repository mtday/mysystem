#!/usr/bin/env bash

cd $(dirname $0)

mvn exec:java -pl :mysystem-system -Dexec.mainClass="mysystem.system.run.Runner"
