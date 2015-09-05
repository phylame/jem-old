@ECHO OFF
REM ----------------------------------------------------------------------------
REM Copyright 2014-2015 Peng Wan <phylame@163.com>
REM
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with the License.
REM You may obtain a copy of the License at
REM
REM     http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.
REM ----------------------------------------------------------------------------

REM Get the Jem home directory
if "%JEM_HOME%" == "" (set "JEM_HOME=%~dp0..")

REM Run Jem CLI
java -jar "%JEM_HOME%\lib\scj-1.1.0.jar" %*
