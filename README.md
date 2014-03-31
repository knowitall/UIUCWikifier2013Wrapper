UIUCWikifier2013Wrapper
=======================
Uses of this code must adhere to the UIUC's License agreement for their Wikifier software here http://cogcomp.cs.illinois.edu/page/software_view/Wikifier

Place UIUC's jar from the dist folder in the lib folder in this directory.

The Main Class
edu.washington.cs.knowitall.UIUCWikifier2013Wrapper.Wikifier can be run like this:

sbt "run-main edu.washington.cs.knowitall.UIUCWikifier2013Wrapper.Wikifier [path/to/UIUCWikifier/Resources/] [ConfigDescription] [/path/to/Input/Dir/Containing/InputFiles] [/path/to/target/OutputDir]"

ConfigDecription can be either FULL,STAND_ALONE_GUROBI, or STAND_ALONE_NO_INFERENCE

In order to use FULL or STAND_ALONE_GUROBI the user must obtain a Gurobi License and install the software on their computer. http://www.gurobi.com/


### Run without Gurobi

* Download UIUC Wikifier Resources from http://cogcomp.cs.illinois.edu/page/software_view/Wikifier
* Run Command `sbt "run-main edu.washington.cs.knowitall.UIUCWikifier2013Wrapper.Wikifier /path/To/Wikifier/Resource/Dir STAND_ALONE_NO_INFERENCE /path/To/Directory/With/Input/Files /path/To/Directory/For/OutputFiles`
