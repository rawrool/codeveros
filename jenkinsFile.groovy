def servicePath = 'services/ui/angular'
def imageRepo = 'rawrool/ui'

node {
    stage('cleanUp') {
        cleanWs()
    }

    checkout scm

    dir(servicePath) {
/*
        stage('dependencies') {
            docker.image('node:14.16').inside {
                sh 'npm ci --quiet --cache="./npm"'
            }
        }
        
        stage('build') {
            docker.image('node:14.16').inside {
                sh 'npm run build.production --cache="./npm"'
            }
        }
/*
        stage('lint') {
            try {
                echo 'linting'
            } catch (Exception e) {
                echo 'Failed linting' + e.toString()
            }
        }

        stage('test') {
            docker.image('buildkite/puppeteer:8.0.0').inside {
                sh 'npm run test --cache="./npm"'
            }
        }
        */
        stage('deliver') {
            if(env.BRANCH_NAME == 'develop') {
                docker.withRegistry('', 'docker') {
                    def myImage = docker.build("${imageRepo}:${env.BUILD_ID}")
                    myImage.push()
                    myImage.push('dev')
                }

                build job: 'deploy', parameters: [string(name: 'env', value: 'dev'), string(name: 'tag', value: 'dev')]
            }
        }

        stage('promote') {
            if(env.BRANCH_NAME == 'master') {
                docker.withRegistry('', 'docker') {
                    def myImage = docker.image("${imageRepo}:dev")
                    myImage.push()
                    myImage.push('latest')
                }

                build job: 'deploy', parameters: [string(name: 'env', value: 'prod'), string(name: 'tag', value: 'latest')]
            }
        }
    }
}
