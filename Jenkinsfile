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
    image: maven:3-jdk-8-alpine
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
          sh 'whoami'
          sh 'ls -la ~'
          sh 'pwd'
          sh 'printenv'
          sh 'ls -la /home'
          sh 'ls -la /root'
          sh 'mvn -Duser.home=/ -P gradle clean compile'
        }
      }
    }
  }
}
