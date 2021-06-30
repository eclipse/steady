Add the following to the `<profiles>` section of the `pom.xml` of your application project.


In case of aggregated, multi-module Maven projects with modules inheriting from their parent, it is sufficient to include the Steady profile in the top-level (parent) POM. If a module does not inherit from the parent, the Steady profile has to be added to its POM file. See [project aggregation and project inheritance](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html) for more details.

The Steady profile contains a configuration for the maven-surefire-plugin. If you use this module already with specific settings in your default profile, you need to add those settings, e.g., the `<argLine>`, also to its configuration in the Steady profile. 
You may want to replace `localhost` with the host name of your vulas backend as well as provide values for the placeholders `<WORKSPACE-TOKEN>`

```xml
    <profile>
        <id>vulas</id>
        <activation>
            <property>
                <name>vulas</name>
            </property>
        </activation>
        <properties>
            <vulas.version>3.0.10-SNAPSHOT</vulas.version>
            <vulas.shared.backend.serviceUrl>http://localhost:8033/backend</vulas.shared.backend.serviceUrl>
            <vulas.core.space.token><WORKSPACE-TOKEN></vulas.core.space.token>
            <vulas.core.appContext.group>${project.groupId}</vulas.core.appContext.group>
            <vulas.core.appContext.artifact>${project.artifactId}</vulas.core.appContext.artifact>
            <vulas.core.appContext.version>${project.version}</vulas.core.appContext.version>
        </properties>
        <build>
            <plugins>
            
                <!-- Copies Steady JARs to ${project.build.directory}/vulas/lib and incl, 
                    which is needed for the instrumentation of JUnit tests. -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-dependency-plugin</artifactId>
                    <version>2.10</version>
                    <executions>
                        <execution>
                            <id>copy-vulas</id>
                            <phase>generate-test-resources</phase>
                            <goals>
                                <goal>copy</goal>
                            </goals>
                            <configuration>
                                <artifactItems>
                                    <artifactItem>
                                        <groupId>org.eclipse.steady</groupId>
                                        <artifactId>lang-java</artifactId>
                                        <version>${vulas.version}</version>
                                        <type>jar</type>
                                        <classifier>jar-with-dependencies</classifier>
                                        <outputDirectory>${project.build.directory}/vulas/lib</outputDirectory>
                                        <destFileName>vulas-core-latest-jar-with-dependencies.jar</destFileName>
                                    </artifactItem>
                                    <artifactItem>
                                        <groupId>org.eclipse.steady</groupId>
                                        <artifactId>lang-java</artifactId>
                                        <version>${vulas.version}</version>
                                        <type>jar</type>
                                        <classifier>jar-with-dependencies</classifier>
                                        <outputDirectory>${project.build.directory}/vulas/include</outputDirectory>
                                        <destFileName>vulas-core-latest-jar-with-dependencies.jar</destFileName>
                                    </artifactItem>
                                </artifactItems>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>

                <!-- Registers the Steady agent at JVM startup (and specifies a couple of configuration settings) -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <version>2.21.0</version>
                    <configuration>
                        <!-- If set to 0, no separate JVM process will be spawned, i.e., one 
                            can use mvnDebug and JVM params can be added to the Maven (rather than putting 
                            them in the Surefire plugin configuration) (default: 1) -->
                        <forkCount>1</forkCount>

                        <!-- Kill the forked test process after a certain number of seconds. 
                            If set to 0, wait forever for the process, never timing out. This allows 
                            Steady to (hopefully) upload all info in its shutdown hook. More info: https://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html, 
                            https://maven.apache.org/surefire/maven-surefire-plugin/examples/shutdown.html -->
                        <forkedProcessTimeoutInSeconds>0</forkedProcessTimeoutInSeconds>
                        
                        <!-- Available as of 2.20 (or so), default is 30 -->
                        <forkedProcessExitTimeoutInSeconds>300</forkedProcessExitTimeoutInSeconds>

                        <!-- Note: System properties cannot be set at the Maven command line, 
                            they are not passed on to the JVM spawned by Surefire (if any, see parameter 
                            forkCount) -->
                        <argLine>
                            -javaagent:"${project.build.directory}/vulas/lib/vulas-core-latest-jar-with-dependencies.jar"
                            -Dvulas.shared.backend.serviceUrl=${vulas.shared.backend.serviceUrl}
                            -Dvulas.shared.tmpDir=${project.build.directory}/vulas/tmp
                            -Dvulas.core.backendConnection=READ_ONLY
                            -Dvulas.core.uploadDir=${project.build.directory}/vulas/upload
                            -Dvulas.core.monitor.periodicUpload.enabled=false
                            -Dvulas.core.appContext.group=${vulas.core.appContext.group}
                            -Dvulas.core.appContext.artifact=${vulas.core.appContext.artifact}
                            -Dvulas.core.appContext.version=${vulas.core.appContext.version}
                            -Dvulas.core.instr.writeCode=false
                            -Dvulas.core.instr.maxStacktraces=10
                            -Dvulas.core.space.token=${vulas.core.space.token}
                            -Dvulas.core.instr.instrumentorsChoosen=org.eclipse.steady.java.monitor.trace.SingleTraceInstrumentor
                            -noverify

                            <!-- Uncomment to write the heap to disk in case of memory issues -->
                            <!-- -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${project.build.directory}/vulas/tmp -->

                            <!-- Uncomment to debug the test execution or the Steady plugin -->
                            <!-- -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -->

                        </argLine>
                        <!-- Exclude problematic tests -->
                        <excludes>
                            <exclude>**/doesnotexist.java</exclude>
                        </excludes>
                    </configuration>
                </plugin>
                
                <plugin>
                    <groupId>org.eclipse.steady</groupId>
                    <artifactId>plugin-maven</artifactId>
                    <version>${vulas.version}</version>
                    <configuration>
                        <layeredConfiguration>
                            <vulas.shared.backend.serviceUrl>${vulas.shared.backend.serviceUrl}</vulas.shared.backend.serviceUrl>
                            <vulas.core.space.token>${vulas.core.space.token}</vulas.core.space.token>
                            
                            <vulas.core.appContext.group>${vulas.core.appContext.group}</vulas.core.appContext.group>
                            <vulas.core.appContext.artifact>${vulas.core.appContext.artifact}</vulas.core.appContext.artifact>
                            <vulas.core.appContext.version>${vulas.core.appContext.version}</vulas.core.appContext.version>

                            <vulas.shared.tmpDir>${project.build.directory}/vulas/tmp</vulas.shared.tmpDir>
                            <vulas.core.uploadDir>${project.build.directory}/vulas/upload</vulas.core.uploadDir>                   
                            <vulas.core.app.sourceDir>${project.build.directory}/classes,${project.basedir}/src/main/java,${project.basedir}/src/main/python</vulas.core.app.sourceDir>

                            <!-- vulas:instr : Instruments JAR/WAR files found in source dir, 
                                and writes to target dir. Files in include dir are put into /WEB-INF/lib 
                                of output WARs. Files in lib dir are part of the class path when instrumenting. -->
                            <vulas.core.instr.sourceDir>${project.build.directory}</vulas.core.instr.sourceDir>
                            <vulas.core.instr.targetDir>${project.build.directory}/vulas/target</vulas.core.instr.targetDir>
                            <vulas.core.instr.includeDir>${project.build.directory}/vulas/include</vulas.core.instr.includeDir>
                            <vulas.core.instr.libDir>${project.build.directory}/vulas/lib</vulas.core.instr.libDir>
                            <vulas.core.instr.writeCode>false</vulas.core.instr.writeCode>
                            <vulas.core.instr.searchRecursive>false</vulas.core.instr.searchRecursive>

                            <!-- vulas:a2c/t2c : Performs static call graph analysis -->
                            <vulas.reach.wala.callgraph.reflection>NO_FLOW_TO_CASTS_NO_METHOD_INVOKE</vulas.reach.wala.callgraph.reflection>
                            <vulas.reach.timeout>60</vulas.reach.timeout>
                            
                            <!-- vulas:report -->
                            <vulas.report.exceptionExcludeBugs></vulas.report.exceptionExcludeBugs>
                            <vulas.report.reportDir>${project.build.directory}/vulas/report</vulas.report.reportDir>
                        </layeredConfiguration>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
```

**Optional**: Binding the Steady plugin goals to the Maven life cycle makes it very easy to invoke the goals in the right order and at the right time (they are all triggered when running `mvn -Dvulas verify`). In order to do so, one has to include the following XML snippet at the end of the configuration section of the Steady plugin in the POM file:

```xml
	<!-- Constraints: (a) App must run after compile, a2c after app, (b) t2c must be run after Junit and integration tests (if any) and the upload of traces -->
	<executions>
		<execution>
			<id>vulas-clean-app-a2c</id>
			<phase>process-classes</phase>
			<goals>
				<goal>clean</goal>
				<goal>app</goal>
				<goal>a2c</goal>
			</goals>
		</execution>
		<execution>
			<id>vulas-upload-t2c</id>
			<phase>verify</phase>
			<goals>
				<goal>upload</goal>
				<goal>t2c</goal>
			</goals>
		</execution>
	</executions>
```