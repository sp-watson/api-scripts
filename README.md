# API client scripts

# Introduction
This project provides Kotlin code to coordinate API calls.

As such, it is very useful for one-off tasks like migrations.

# Features
There is currently only 1 function, invoked from the Main class.

It reads offender details from an XLSX spreadsheet (the format of which has been determined by another team).
These offender details are then used to recall then register them as a restricted patient.

The Main class defines all the configuration and wires up the dependencies.

There are 3 outputs and 1 input file:
* The console, which gives messages indicating what is happening
* A summary file called "SUMMARY-[date and time].txt". This provides a list of the outcomes of the migration for each offender.
* A file for each offender. This gives information about how far the migration got and any error messages.
  If a file already exists for a givne offender, then that is moved to the archive sub-directory.
* A file that contains a list of offenders that have been successfully migrated. This must exist, so before the first run must be created and be empty.
  This file should never be deleted, as it is the only way the system knows whether to try to migrate an offender.
* A file that contains a list of offenders that have been successfully recalled.
  As per the above "successfully migrated" file, his has to exist.

## Instructions
In order to run the feature you will need to:
* Create a class that implements the Config class
* As part of that class, add a name of a base directory `resultsBaseDirectory`
* In that base directory:
  - Make a blank sub-directory called "archive"
  - Make a blank file called SUCCESSFUL_MIGRATIONS.txt
  - Make a blank file called SUCCESSFUL_RECALLS.txt
* Add config `spreadsheetFileName` that indicates where the xlsx file is
* In the Main class, set the first offender number (e.g. 1 - start of the file) and the number of offenders
* Run it using the Main class as the entry point