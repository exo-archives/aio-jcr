@echo off

@rem @SET JAVA_HOME=%JROCKIT_HOME%

@rem ===== For debug use, as is. Peter Nedonosko =====

@cls

@echo USAGE:     Params
@echo USAGE:     -a, --all      build all: root, plugin, exo-platform, exo-jcr
@echo USAGE:     -t, --test     enable tests (Don't works properly for jcr-impl from this dir, need impl dir)
@echo USAGE:     -at --alltest      build all, enable tests
@echo USAGE:     -f filename        modules.xml file name in v2.x dir
@echo USAGE:     without params     disable tests

@rem switch on/off tests param

@set runtests=-Dmaven.test.skip=true

@if "%1"=="--test" goto setTestOn
@if "%1"=="--alltest" goto setTestOn
@if "%1"=="-t" goto setTestOn
@if "%1"=="-at" goto setTestOn
@echo ===================================== TESTS DISABLED ==============================================
goto checkBuildAll

:setTestOn
@set runtests=-Dmaven.test.skip=false
@echo ===================================== TESTS ENABLED ===============================================

:checkBuildAll
@if "%1"=="" goto jcrBuild
@if "%1"=="-t" goto jcrBuild

@echo ===================================================================================================
@echo ========== Building project root, maven plugin, exo-platform, exo-jcr (modules-jcr.xml) ===========
@echo ===================================================================================================

@set modules=modules-jcr.xml
@if "%1"=="-f" @set modules=%2
@if "%2"=="-f" @set modules=%3
@if "%3"=="-f" @set modules=%4

@call mvn %runtests% -Dtest.repository=db1 -f %modules% clean install

@goto done

:jcrBuild
@echo ===================================================================================================
@echo ================================= Building exo-jcr ================================================
@echo ===================================================================================================
@start mvn %runtests% -Dtest.repository=db1 clean install

:done
@echo ===================================================================================================
@echo =============================================== Done ==============================================
@echo ===================================================================================================
