rem @echo off
rem ---------------------------------------------------------------------------
rem Batch Program to run the SMGUI as a stand alone application
rem ---------------------------------------------------------------------------
rem Notes:	(1)	This batch file is updated during the installation
rem			process, with the environment settings taken from
rem			the process, as follows:
rem
rem			HOMED, set from HOMED _ TOKEN during install (defaulted
rem				to the ??? registry variable, if available or
rem				the user's choice otherwise).
rem
rem		(2)	WARNING, do not modify this batch file by hand unless
rem			you know what you are doing!  The CWRAT documentation
rem			describes the command-line arguments.  If CWRAT does
rem			not run, try using "jre" instead of "jrew" so that
rem			you can see the output from the SMGUI process.
rem ---------------------------------------------------------------------------
rem History:
rem
rem 06 May 1998	Catherine E. Nutting	Copied original version from CWRAT,
rem		Riverside Technology,	modified for SMGUI
rem		inc.
rem 27 Apr 1999	CEN, RTi		Updated to use jar files;
rem					Reference JRE _ TOKEN
rem 08 Nov 2000 CEN, RTi		Added -home variable (directories
rem					such as logs and system are below this)
rem 21 Aug 2001	Steven A. Malers, RTi	Remove Visualize package from classpath
rem					since it is not used for graphing.
rem					Set variables to empty at end.
rem 2002-07-02	SAM, RTi		Remove browser and documentation
rem					settings (determined automatically now).
rem ---------------------------------------------------------------------------

SET HOMED=\CDSS
rem SET JREHOMED=\Java_CO
SET JREHOMED=\CDSS\JRE_118
Set JAR_HOMED=%HOMED%\bin

rem Run using the development environment...
%JREHOMED%\bin\jre -mx128m -Djava.compiler=NONE -cp ..\..\..\classes_118;\cdss\develop\classes_118;I:\develop\classes_118;%JAR_HOMED%\HBGUI.jar;%JAR_HOMED%\Symantec_118.jar;%JAR_HOMED%\RogueWave_118.jar; DWR.SMGUI.smgui -home %HOMED% %1 %2 %3 %4 %5 %6 %7

Set HOMED=
Set JREHOMED=
Set JAR_HOMED=
