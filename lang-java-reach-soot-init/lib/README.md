# Soot CallGraph Construction Service: Soot & Soot-Infoflow 

## Build Soot Callgraph Constructor Service
To build the Soot CallGraph Construction Service invoke:

 
    mvn clean compile install -Psoot


During the build the following libraries are downloaded automatically:
 - [Soot](https://github.com/Sable/soot) 3.1.0 is downloaded from <https://soot-build.cs.uni-paderborn.de/nexus/repository/soot-release/> , [LGPL-2.1](https://github.com/Sable/soot/blob/develop/LICENSE.txt)
 - [Soot-Infoflow](https://github.com/secure-software-engineering/FlowDroid/tree/master/soot-infoflow) from <https://github.com/secure-software-engineering/FlowDroid/releases/download/v2.6/soot-infoflow-classes.jar> [LGPL-2.1](https://github.com/secure-software-engineering/FlowDroid/blob/master/soot-infoflow/license.txt)



