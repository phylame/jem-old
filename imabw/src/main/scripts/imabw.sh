#!/bin/bash
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

# Get the Imabw home
if [ -z "$IMABW_HOME" -o ! -d "$IMABW_HOME" ]; then
  PRG="$0"
  # need this for relative symlinks
  while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG=`dirname "$PRG"`"/$link"
    fi
  done

  IMABW_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  IMABW_HOME=`cd "$IMABW_HOME" > /dev/null && pwd`
fi

# Set extension JAR
EXT_LIB=""
EXT_DIR="$IMABW_HOME"/lib/ext
if [ -d "$EXT_DIR" ]; then
  for f in "$EXT_DIR"/*.jar; do
    EXT_LIB="$EXT_LIB:$f"
  done
  if [ -n "$EXT_LIB" ]; then
    len=`expr length "$EXT_LIB"`
    EXT_LIB=`expr substr "$EXT_LIB" 2 "$len"`
  fi
fi

# Run Jem SCI
java -Xbootclasspath/a:"$EXT_LIB" -jar "$IMABW_HOME/lib/imabw-2.2.1.jar" "$@"
