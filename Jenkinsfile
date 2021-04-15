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
    image: eclipse/steady-pipeline
    command:
    - cat
    tty: true
    env:
    - name: "MAVEN_OPTS"
      value: "-Duser.home=/home/jenkins -Xmx4096m -Xms4096m"
    - name: "MAVEN_CONFIG"
      value: "/home/jenkins/.m2"
    volumeMounts:
    - name: settings-xml
      mountPath: /home/jenkins/.m2/settings.xml
      subPath: settings.xml
      readOnly: true
    - name: m2-repo
      mountPath: /home/jenkins/.m2/repository
    resources:
      limits:
        memory: "2Gi"
        cpu: "1"
      requests:
        memory: "2Gi"
        cpu: "1"
  volumes:
  - name: settings-xml
    secret:
      secretName: m2-secret-dir
      items:
      - key: settings.xml
        path: settings.xml
  - name: m2-repo
    emptyDir: {}
"""
    }
  }
  stages {
    stage('Compile') {
      steps {
        container('maven') {
          sh 'mvn -P gradle -Dvulas.shared.m2Dir=/home/jenkins/agent/workspace -Dspring.standalone -Dit.test="!IT01_PatchAnalyzerIT, IT*, *IT, *ITCase" -DfailIfNoTests=false clean test'
        }
      }
    }
  }
}
