#!/usr/local/bin/bash
#!/bin/bash
APP="tg-bot-1.0-SNAPSHOT.jar"
export JAVA_VERSION="21"

run_dir=$(dirname $(readlink -f "$0"))
export HANLP_ROOT=${run_dir}
cd $run_dir
time=$(date "+%Y-%m-%d %H:%M:%S")
echo ----------------------------$time----------------- >> start.log
echo "" >> start.log
./run.sh start $APP >> start.log
echo "" >> start.log
echo ----------------------------$time----------------- >> start.log
echo "" >> start.log
echo "" >> start.log

echo "" >> start.log
echo "" >> start.log
echo "" >> start.log
echo "" >> start.log
echo "" >> start.log
echo "" >> start.log
echo "" >> start.log
echo "" >> start.log
echo "" >> start.log
echo "" >> start.log


