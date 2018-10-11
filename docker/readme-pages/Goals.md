## About goals

The various client-side tools offer so-called **goals** in order to analyze applications and interact with the backend.

The following goals perform some sort of application analysis:
- **app**: Creates a method-level bill of material of an application and all its dependencies
- **a2c**: Builds a call graph (starting from app methods) and checks whether vulnerable code is reachable
- **test**: Collects execution traces during JUnit tests
- **t2c**: Builds a call graph (starting from traced methods) and checks whether vulnerable code is reachable

The following goals are related to data management and reporting:
- **upload**: Uploads analysis data previously written to disk to the backend
- **report**: Downloads analysis data from the backend to the client, produces a result report (Html, Xml, Json), and throws a build exception in order to break Jenkins jobs
- **clean**: Cleans the analysis data of a single app in the backend
- **cleanSpace**: Cleans an entire workspace in the backend

Which goals are supported by the different clients, and how-to execute them is explained in the [Java](Java.md) and [Python](Python.md) sections.

## Important: Read before use

Make sure to understand the following before analyzing an application and interpreting (assessing) the findings:

### _app_ has to be executed before all other analysis goals

After running _app_, all vulnerable application dependencies are known, and the analysis goals _a2c_, _test_ and _t2c_ can be used to collect evidence concerning the execution of vulnerable code.

### Once _app_ has been run, the assessment of analysis results (findings) can start

Each finding of _app_ corresponds to a dependency of an application on a component with a known security vulnerability. The number of findings will not change when running other analysis goals. Instead, _a2c_, _test_ and _t2c_ try to collect additional information for all of the findings brought up by _app_ (regarding the execution of vulnerable code).

### Assess every finding, no matter whether _a2c_, _test_ and _t2c_ were able to collect evidence or not

Not finding such evidence does not mean that vulnerabilities cannot be exploited. The absence of proof is not a proof of absence (of exploitable vulnerabilities).
