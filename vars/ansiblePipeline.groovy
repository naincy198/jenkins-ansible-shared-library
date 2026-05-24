def call() {

    def config = loadConfig()

    pipeline {
        agent any

        stages {

            stage('Clone') {
    steps {
        script {
            echo "Cloning Repository..."

            git branch: 'main',
                credentialsId: 'ad97fd49-b7b6-4c1b-a4c2-dc8bc80a4d81',
                url: config.GIT_REPO_URL
        }
    }
}
                
                  
           stage('User Approval') {

                when {
                    expression {
                        config.KEEP_APPROVAL_STAGE.toBoolean()
                    }
                }

                steps {
                    input "Approve Deployment?"
                }
            }

            stage('Playbook Execution') {
    steps {
        script {
            dir('ansible-project') {
                sh '''
                export PATH=/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:$PATH
                which ansible-playbook
                ansible-playbook --version
                ansible-playbook -i env/prod/inventory env/prod/playbook.yml
                '''
            }
        }
    }
}
               
            stage('Notification') {
                steps {

                    script {

                        slackSend(
                            channel: config.SLACK_CHANNEL_NAME,
                            message: "${config.ACTION_MESSAGE} - ${config.ENVIRONMENT}"
                        )
                    }
                }
            }
        }
    }
}

def loadConfig() {

    def props = new Properties()

    def resourceFile = libraryResource 'config.properties'

    props.load(new StringReader(resourceFile))

    return props
}
