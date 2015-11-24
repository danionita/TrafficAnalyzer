#!/bin/bash
TOOL=$1
shift
echo $TOOL >> src/main/resources/tools.txt
sed -e "s/TemplateTool/$TOOL/g" src/main/java/nl/utwente/bigdata/TemplateTool.java > src/main/java/nl/utwente/trafficanalyzer/$TOOL.java
