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
    image: maven:3-adoptopenjdk-8
    command:
    - cat
    tty: true
"""
    }
  }
  stages {
    stage('Compile') {
      steps {
        container('maven') {
          sh 'mkdir -p /home/jenkins/agent/workspace/.m2'
          sh 'export MAVEN_CONFIG=/home/jenkins/agent/workspace/.m2'
          sh 'export MAVEN_OPTS="-Xmx4096m -Xms4096m -XX:MaxPermSize=1024m"'
          sh 'mvn -Duser.home=/home/jenkins/agent/workspace -P gradle clean compile'
        }
      }
    }
  }
}
