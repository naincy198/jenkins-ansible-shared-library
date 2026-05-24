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
                            credentialsId: 'github-ssh',
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

                        echo "Executing Ansible Playbook..."

                        sh """
                        ansible-playbook ${config.CODE_BASE_PATH}/playbook.yml \
                        -i ${config.CODE_BASE_PATH}/inventory
                        """
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
