```xml hl_lines="9 10 11"
    <profile>
        <id>steady</id>
        <activation>
            <property>
                <name>steady</name>
            </property>
        </activation>
        <properties>
            <vulas.version>@@PROJECT_VERSION@@</vulas.version>
            <vulas.shared.backend.serviceUrl>@@ADDRESS@@/backend/</vulas.shared.backend.serviceUrl>
            <vulas.core.space.token>WORKSPACE_TOKEN</vulas.core.space.token>
            <vulas.core.appContext.group>${project.groupId}</vulas.core.appContext.group>
            <vulas.core.appContext.artifact>${project.artifactId}</vulas.core.appContext.artifact>
            <vulas.core.appContext.version>${project.version}</vulas.core.appContext.version>
        </properties>
        <build>
            <plugins>
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

                            <!-- steady:instr : Instruments JAR/WAR files found in source dir,
                                and writes to target dir. Files in include dir are put into /WEB-INF/lib
                                of output WARs. Files in lib dir are part of the class path when instrumenting. -->
                            <vulas.core.instr.sourceDir>${project.build.directory}</vulas.core.instr.sourceDir>
                            <vulas.core.instr.targetDir>${project.build.directory}/vulas/target</vulas.core.instr.targetDir>
                            <vulas.core.instr.includeDir>${project.build.directory}/vulas/include</vulas.core.instr.includeDir>
                            <vulas.core.instr.libDir>${project.build.directory}/vulas/lib</vulas.core.instr.libDir>
                            <vulas.core.instr.writeCode>false</vulas.core.instr.writeCode>
                            <vulas.core.instr.instrumentorsChoosen>org.eclipse.steady.java.monitor.trace.SingleTraceInstrumentor</vulas.core.instr.instrumentorsChoosen>
                            <vulas.core.instr.searchRecursive>false</vulas.core.instr.searchRecursive>
                            <vulas.core.monitor.periodicUpload.enabled>false</vulas.core.monitor.periodicUpload.enabled>
                            <vulas.core.instr.maxStacktraces>10</vulas.core.instr.maxStacktraces>

                            <!-- steady:a2c/t2c : Performs static call graph analysis -->
                            <vulas.reach.wala.callgraph.reflection>NO_FLOW_TO_CASTS_NO_METHOD_INVOKE</vulas.reach.wala.callgraph.reflection>
                            <vulas.reach.timeout>60</vulas.reach.timeout>

                            <!-- steady:report -->
                            <vulas.report.reportDir>${project.build.directory}/vulas/report</vulas.report.reportDir>
                        </layeredConfiguration>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
```
