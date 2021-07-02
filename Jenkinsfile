pipeline {
  agent {
    kubernetes {
      label 'my-agent-pod'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: eclipse/steady-pipeline:latest
    command:
    - cat
    tty: true
    env:
    - name: "MAVEN_OPTS"
      value: "-Duser.home=/home/jenkins -Xmx4096m -Xms4096m"
    - name: "MAVEN_CONFIG"
      value: "/home/jenkins/.m2"
    - name: "GNUPGHOME"
      value: "/home/jenkins/.gnupg"
    volumeMounts:
    - name: settings-xml
      mountPath: /home/jenkins/.m2/settings.xml
      subPath: settings.xml
      readOnly: true
    - name: toolchains-xml
      mountPath: /home/jenkins/.m2/toolchains.xml
      subPath: toolchains.xml
      readOnly: true
    - name: settings-security-xml
      mountPath: /home/jenkins/.m2/settings-security.xml
      subPath: settings-security.xml
      readOnly: true
    - name: m2-repo
      mountPath: /home/jenkins/.m2/repository
    - name: gnupg-vol
      mountPath: /home/jenkins/.gnupg
    resources:
      limits:
        memory: "4Gi"
        cpu: "2"
      requests:
        memory: "4Gi"
        cpu: "2"
  volumes:
  - name: settings-xml
    secret:
      secretName: m2-secret-dir
      items:
      - key: settings.xml
        path: settings.xml
  - name: toolchains-xml
    configMap:
      name: m2-dir
      items:
      - key: toolchains.xml
        path: toolchains.xml
  - name: settings-security-xml
    secret:
      secretName: m2-secret-dir
      items:
      - key: settings-security.xml
        path: settings-security.xml
  - name: m2-repo
    emptyDir: {}
  - name: gnupg-vol
    emptyDir: {}
"""
    }
  }
  stages {
    // Verifies that no Spotbugs checks fails (cf.
    // https://eclipse.github.io/steady/contributor/#contribution-content-guidelines).
    stage('Verify Spotbugs') {
      steps {
        container('maven') {
          sh 'mvn -B -e -P gradle -Dvulas.shared.m2Dir=/home/jenkins/agent/workspace -Dspring.standalone \
              -Dspotbugs.excludeFilterFile=findbugs-exclude.xml -Dspotbugs.includeFilterFile=findbugs-include.xml \
              -Dspotbugs.failOnError=true -DskipTests clean install com.github.spotbugs:spotbugs-maven-plugin:4.2.3:check'
        }
      }
    }
    // Verifies that the -javadoc and -sources artifacts can be generated (by enabling the
    // javadoc profile contained in three pom.xml files). Also verifies that the build
    // is reproducible.
    stage('Verify JavaDoc, Source and Reproducibility') {
      steps {
        container('maven') {
          sh 'mvn -B -e -P gradle,javadoc -Dspring.standalone -DskipTests clean install'
          sh 'mvn -B -e -P gradle,javadoc -Dspring.standalone -DskipTests -Dreference.repo=https://repo.maven.apache.org/maven2 clean verify'
          sh 'cat target/root-*.buildinfo.compare'
          sh 'grep ko=0 target/root-*.buildinfo.compare' // Fail if JARs are different
        }
      }
    }
    // Verifies compliance with Google's Java Style Guide (cf.
    // https://eclipse.github.io/steady/contributor/#contribution-content-guidelines).
    stage('Verify Coding Style and REUSE compliance') {
      steps {
        container('maven') {
          sh 'bash .travis/check_code_style.sh'
          sh 'reuse lint'
        }
      }
    }
    // Verifies that all tests pass (except for expensive patch analyses).
    stage('Test') {
      steps {
        container('maven') {
          sh 'mvn -B -e -P gradle -Dvulas.shared.m2Dir=/home/jenkins/agent/workspace -Dspring.standalone \
              -Dit.test="!IT01_PatchAnalyzerIT,IT*,*IT" -DfailIfNoTests=false clean test'
        }
      }
    }
    // GPG signs all artifacts and deploys them on Maven Central. See here for
    // additional info: https://www.jenkins.io/doc/book/pipeline/syntax/,
    // https://wiki.eclipse.org/Jenkins
    stage('Release on Central') {
      // when { branch "sign-releases" }
      when { tag "release-*" }
      steps {
        container('maven') {
          echo "Branch [${env.BRANCH_NAME}], tag [${env.TAG_NAME}]"
          withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING')]) {
            sh 'gpg --batch --import "${KEYRING}"'
            sh 'for fpr in $(gpg --list-keys --with-colons  | awk -F: \'/fpr:/ {print $10}\' | sort -u); do echo -e "5\ny\n" |  gpg --batch --command-fd 0 --expert --edit-key ${fpr} trust; done'
          }
          sh 'mvn -B -e -Dspring.standalone -DskipTests -P gradle,javadoc,release clean deploy'
        }
      }
    }
  }
}
