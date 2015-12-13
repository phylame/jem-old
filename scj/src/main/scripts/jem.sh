#!/usr/bin/env bash
# ------------------------------------------------------------------------------
# Copyright 2014-2015 Peng Wan <phylame@163.com>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ------------------------------------------------------------------------------

# Get the SCJ home
if [ -z "$SCJ_HOME" -o ! -d "$SCJ_HOME" ]; then
  PRG="$0"
  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG=`dirname "$PRG"`"/$link"
    fi
  done

  SCJ_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  SCJ_HOME=`cd "$SCJ_HOME" > /dev/null && pwd`
fi

# Set extension JAR
for i in "%SCJ_HOME%"\lib\ext\*.jar
do
  set EXTENSION_JAR="$i"
done

# Run Jem SCI
java -Xbootclasspath/a:%EXTENSION_JAR% -jar "%SCJ_HOME%\lib\scj-1.3.jar" "$@"
