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
    stage('Spotbugs') {
      steps {
        container('maven') {
          sh 'mvn -e -P gradle -Dvulas.shared.m2Dir=/home/jenkins/agent/workspace -Dspring.standalone \
              -Dspotbugs.excludeFilterFile=findbugs-exclude.xml -Dspotbugs.includeFilterFile=findbugs-include.xml \
              -Dspotbugs.failOnError=true -DskipTests clean install com.github.spotbugs:spotbugs-maven-plugin:4.2.3:check'
        }
      }
    }
    // Verifies that the Javadoc documentation can be generated (by enabling the
    // javadoc profile contained in several pom.xml files).
    stage('JavaDoc') {
      steps {
        container('maven') {
          sh 'mvn -e -P gradle,javadoc -Dspring.standalone -DskipTests clean package'
        }
      }
    }
    // Verifies that all tests pass (except for expensive patch analyses).
    stage('Tests') {
      steps {
        container('maven') {
          sh 'mvn -e -P gradle -Dvulas.shared.m2Dir=/home/jenkins/agent/workspace -Dspring.standalone \
              -Dit.test="!IT01_PatchAnalyzerIT,IT*,*IT" -DfailIfNoTests=false clean test'
        }
      }
    }
    // Verifies compliance with Google's Java Style Guide (cf.
    // https://eclipse.github.io/steady/contributor/#contribution-content-guidelines).
    stage('Codestyle') {
      steps {
        container('maven') {
          sh 'bash .travis/check_code_style.sh'
        }
      }
    }
    // Verifies that artifacts can be signed with GPG (required for releases on Maven Central).
    // TODO: Complete in separate PR.
    stage('Sign') {
      steps {
        container('maven') {
          sh 'gpg --version'
          withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING')]) {
            sh 'gpg --batch --import "${KEYRING}"'
            sh 'for fpr in $(gpg --list-keys --with-colons  | awk -F: \'/fpr:/ {print $10}\' | sort -u); do echo -e "5\ny\n" |  gpg --batch --command-fd 0 --expert --edit-key ${fpr} trust; done'
          }
        }
      }
    }
  }
}
