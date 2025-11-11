#!/bin/bash
sudo systemctl stop swp-project
mvn clean package -DskipTests
./run_sql.sh
sudo systemctl start swp-project
