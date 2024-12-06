@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  project-antoni startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and PROJECT_ANTONI_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\project-antoni.jar;%APP_HOME%\lib\plot-api-jvm-4.2.0.jar;%APP_HOME%\lib\ktor-client-cio-jvm-2.0.0.jar;%APP_HOME%\lib\ktor-client-serialization-jvm-2.0.0.jar;%APP_HOME%\lib\ktor-client-json-jvm-2.0.0.jar;%APP_HOME%\lib\ktor-client-core-jvm-2.0.0.jar;%APP_HOME%\lib\lets-plot-common-3.0.0.jar;%APP_HOME%\lib\desktop-jvm-1.5.0.jar;%APP_HOME%\lib\skiko-awt-runtime-linux-x64-0.7.77.jar;%APP_HOME%\lib\material-desktop-1.5.0.jar;%APP_HOME%\lib\material-ripple-desktop-1.5.0.jar;%APP_HOME%\lib\foundation-desktop-1.5.0.jar;%APP_HOME%\lib\material-icons-extended-desktop-1.5.0.jar;%APP_HOME%\lib\material-icons-core-desktop-1.5.0.jar;%APP_HOME%\lib\animation-desktop-1.5.0.jar;%APP_HOME%\lib\foundation-layout-desktop-1.5.0.jar;%APP_HOME%\lib\animation-core-desktop-1.5.0.jar;%APP_HOME%\lib\ui-desktop-1.5.0.jar;%APP_HOME%\lib\ui-text-desktop-1.5.0.jar;%APP_HOME%\lib\ui-graphics-desktop-1.5.0.jar;%APP_HOME%\lib\skiko-awt-0.7.77.jar;%APP_HOME%\lib\ui-tooling-preview-desktop-1.5.0.jar;%APP_HOME%\lib\runtime-saveable-desktop-1.5.0.jar;%APP_HOME%\lib\ui-unit-desktop-1.5.0.jar;%APP_HOME%\lib\ui-geometry-desktop-1.5.0.jar;%APP_HOME%\lib\runtime-desktop-1.5.0.jar;%APP_HOME%\lib\ktor-events-jvm-2.0.0.jar;%APP_HOME%\lib\ktor-websocket-serialization-jvm-2.0.0.jar;%APP_HOME%\lib\ktor-http-cio-jvm-2.0.0.jar;%APP_HOME%\lib\ktor-serialization-jvm-2.0.0.jar;%APP_HOME%\lib\ktor-websockets-jvm-2.0.0.jar;%APP_HOME%\lib\ktor-http-jvm-2.0.0.jar;%APP_HOME%\lib\ktor-network-tls-jvm-2.0.0.jar;%APP_HOME%\lib\ktor-network-jvm-2.0.0.jar;%APP_HOME%\lib\ktor-utils-jvm-2.0.0.jar;%APP_HOME%\lib\ktor-io-jvm-2.0.0.jar;%APP_HOME%\lib\kotlinx-coroutines-jdk8-1.6.4.jar;%APP_HOME%\lib\kotlinx-coroutines-slf4j-1.6.4.jar;%APP_HOME%\lib\kotlinx-coroutines-core-jvm-1.6.4.jar;%APP_HOME%\lib\plot-config-portable-jvm-3.0.0.jar;%APP_HOME%\lib\plot-builder-portable-jvm-3.0.0.jar;%APP_HOME%\lib\plot-base-portable-jvm-3.0.0.jar;%APP_HOME%\lib\plot-common-portable-jvm-3.0.0.jar;%APP_HOME%\lib\vis-svg-portable-jvm-3.0.0.jar;%APP_HOME%\lib\base-portable-jvm-3.0.0.jar;%APP_HOME%\lib\kotlinx-serialization-core-jvm-1.5.0.jar;%APP_HOME%\lib\kotlinx-serialization-json-jvm-1.5.0.jar;%APP_HOME%\lib\kotlin-logging-jvm-2.0.5.jar;%APP_HOME%\lib\lets-plot-image-export-4.2.0.jar;%APP_HOME%\lib\platf-awt-jvm-4.2.0.jar;%APP_HOME%\lib\ui-util-desktop-1.5.0.jar;%APP_HOME%\lib\atomicfu-jvm-0.17.0.jar;%APP_HOME%\lib\kotlin-stdlib-jdk7-1.8.21.jar;%APP_HOME%\lib\kotlin-stdlib-1.9.21.jar;%APP_HOME%\lib\kotlin-stdlib-jdk8-1.8.21.jar;%APP_HOME%\lib\jfreechart-1.5.3.jar;%APP_HOME%\lib\batik-codec-1.17.jar;%APP_HOME%\lib\batik-transcoder-1.17.jar;%APP_HOME%\lib\imageio-tiff-3.8.1.jar;%APP_HOME%\lib\annotations-13.0.jar;%APP_HOME%\lib\slf4j-api-1.7.36.jar;%APP_HOME%\lib\batik-bridge-1.17.jar;%APP_HOME%\lib\batik-script-1.17.jar;%APP_HOME%\lib\batik-anim-1.17.jar;%APP_HOME%\lib\batik-gvt-1.17.jar;%APP_HOME%\lib\batik-svggen-1.17.jar;%APP_HOME%\lib\batik-svg-dom-1.17.jar;%APP_HOME%\lib\batik-parser-1.17.jar;%APP_HOME%\lib\batik-awt-util-1.17.jar;%APP_HOME%\lib\batik-dom-1.17.jar;%APP_HOME%\lib\batik-xml-1.17.jar;%APP_HOME%\lib\batik-css-1.17.jar;%APP_HOME%\lib\batik-util-1.17.jar;%APP_HOME%\lib\batik-ext-1.17.jar;%APP_HOME%\lib\batik-constants-1.17.jar;%APP_HOME%\lib\batik-i18n-1.17.jar;%APP_HOME%\lib\batik-shared-resources-1.17.jar;%APP_HOME%\lib\xml-apis-ext-1.3.04.jar;%APP_HOME%\lib\imageio-metadata-3.8.1.jar;%APP_HOME%\lib\imageio-core-3.8.1.jar;%APP_HOME%\lib\common-image-3.8.1.jar;%APP_HOME%\lib\common-io-3.8.1.jar;%APP_HOME%\lib\common-lang-3.8.1.jar;%APP_HOME%\lib\xmlgraphics-commons-2.9.jar;%APP_HOME%\lib\xml-apis-1.4.01.jar;%APP_HOME%\lib\commons-io-2.11.0.jar;%APP_HOME%\lib\commons-logging-1.0.4.jar


@rem Execute project-antoni
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %PROJECT_ANTONI_OPTS%  -classpath "%CLASSPATH%" MainKt %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable PROJECT_ANTONI_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%PROJECT_ANTONI_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
