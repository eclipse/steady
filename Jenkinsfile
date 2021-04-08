pipeline {
  agent {
    docker {
        image 'maven:3-jdk-8-alpine'
        args '-v $HOME/.m2:/var/maven/.m2:z -e MAVEN_CONFIG=/var/maven/.m2 -e MAVEN_OPTS="-Duser.home=/var/maven"'
    }
  }
  stages {
    stage('Compile') {
      steps {
        sh 'mvn -P gradle clean compile'
      }
    }
  }
}
