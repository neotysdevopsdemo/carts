
pipeline {
    agent  { label 'master' }
    tools {
        maven 'Maven 3.6.0'
        jdk 'jdk8'
    }
    environment {
        VERSION="0.1"
        APP_NAME = "carts"
        TAG = "neotysdevopsdemo/${APP_NAME}"
        TAG_DEV = "${TAG}:DEV-${VERSION}"
        NL_DT_TAG = "app:${env.APP_NAME},environment:dev"
        CARTS_ANOMALIEFILE = "$WORKSPACE/monspec/carts_anomalieDection.json"
        TAG_STAGING = "${TAG}-stagging:${VERSION}"
        DYNATRACEID = "${env.DT_ACCOUNTID}.live.dynatrace.com"
        DYNATRACEAPIKEY = "${env.DT_API_TOKEN}"
        NLAPIKEY = "${env.NL_WEB_API_KEY}"
        OUTPUTSANITYCHECK = "$WORKSPACE/infrastructure/sanitycheck.json"

        DYNATRACEPLUGINPATH = "$WORKSPACE/lib/DynatraceIntegration-3.0.1-SNAPSHOT.jar"
        DOCKER_COMPOSE_TEMPLATE="$WORKSPACE/infrastructure/infrastructure/neoload/docker-compose.template"
        DOCKER_COMPOSE_LG_FILE = "$WORKSPACE/infrastructure/infrastructure/neoload/docker-compose-neoload.yml"

        GROUP = "neotysdevopsdemo"
        COMMIT = "DEV-${VERSION}"

    }
    stages {
        stage('Checkout') {
            agent { label 'master' }
            steps {
                git  url:"https://github.com/${GROUP}/${APP_NAME}.git",
                        branch :'master'
            }
        }
        stage('Maven build') {
            steps {
                sh "mvn -B clean package -DdynatraceId=$DYNATRACEID -DneoLoadWebAPIKey=$NLAPIKEY -DdynatraceApiKey=$DYNATRACEAPIKEY -Dtags=${NL_DT_TAG} -DoutPutReferenceFile=$OUTPUTSANITYCHECK -DcustomActionPath=$DYNATRACEPLUGINPATH -DjsonAnomalieDetectionFile=$CARTS_ANOMALIEFILE"
                // cette ligne est pour license ...mais il me semble que tu as license avec ton container  sh "chmod -R 777 $WORKSPACE/target/neoload/"
            }
        }
        stage('Docker build') {
            when {
                expression {
                    return env.BRANCH_NAME ==~ 'release/.*' || env.BRANCH_NAME ==~ 'master'
                }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerHub', passwordVariable: 'TOKEN', usernameVariable: 'USER')]) {
                    sh "cp ./target/*.jar ./docker/carts"
                    sh "docker build --build-arg BUILD_VERSION=${VERSION} --build-arg COMMIT=$COMMIT -t ${TAG_DEV} $WORKSPACE/docker/carts/"
                    sh "docker login --username=${USER} --password=${TOKEN}"
                    sh "docker push ${TAG_DEV}"
                }

            }
        }
       stage('create docker netwrok') {

                              steps {
                                   sh "docker network create ${APP_NAME} || true"

                              }
               }

        stage('Deploy to dev ') {

            steps {
                sh "sed -i 's,TAG_TO_REPLACE,${TAG_DEV},' $WORKSPACE/docker-compose.yml"
                sh "sed -i 's,TO_REPLACE,${APP_NAME},' $WORKSPACE/docker-compose.yml"
                sh 'docker-compose -f $WORKSPACE/docker-compose.yml up -d'

            }
        }

        stage('Start NeoLoad infrastructure') {

                           steps {
                                      sh "cp -f ${DOCKER_COMPOSE_TEMPLATE} ${DOCKER_COMPOSE_LG_FILE}"
                                      sh "sed -i 's,TO_REPLACE,${APP_NAME},'  ${DOCKER_COMPOSE_LG_FILE}"
                                      sh "sed -i 's,TOKEN_TOBE_REPLACE,$NLAPIKEY,'  ${DOCKER_COMPOSE_LG_FILE}"
                                      sh 'docker-compose -f ${DOCKER_COMPOSE_LG_FILE} up -d'
                                      sleep 15

                                  }

                      }


        stage('Run functional check in dev') {


            steps {

                  sleep 90

                  sh "docker pull neotys/neoload-web-test-launcher:latest"
                  sh "ls -al $WORKSPACE/target/neoload/Carts_NeoLoad/"
                  sh "docker run --rm \
                                     -v $WORKSPACE/target/neoload/Carts_NeoLoad/:/neoload-project \
                                     -e NEOLOADWEB_TOKEN=$NLAPIKEY \
                                     -e TEST_RESULT_NAME=FuncCheck_carts__${VERSION}_${BUILD_NUMBER} \
                                     -e SCENARIO_NAME=Cart_Load \
                                     -e CONTROLLER_ZONE_ID=defaultzone \
                                     -e LG_ZONE_IDS=defaultzone:1 \
                                     --network ${APP_NAME} --user root\
                                      neotys/neoload-web-test-launcher:latest"


                    /*neoloadRun executable: '/home/neoload/neoload/bin/NeoLoadCmd',
                            project: "$WORKSPACE/target/neoload/Carts_NeoLoad/Carts_NeoLoad.nlp",
                            testName: 'FuncCheck_carts__${VERSION}_${BUILD_NUMBER}',
                            testDescription: 'FuncCheck_carts__${VERSION}_${BUILD_NUMBER}',
                            commandLineOption: "-nlweb Population_AddItemToCart=$WORKSPACE/infrastructure/infrastructure/neoload/lg/remote.txt -L Population_Dynatrace_Integration=$WORKSPACE/infrastructure/infrastructure/neoload/lg/local.txt  -nlwebToken $NLAPIKEY -variables carts_host=carts,carts_port=80",
                            scenario: 'Cart_Load', sharedLicense: [server: 'NeoLoad Demo License', duration: 2, vuCount: 200],
                            trendGraphs: [
                                    [name: 'Limit test Carts API Response time', curve: ['AddItemToCart>Actions>AddItemToCart'], statistic: 'average'],
                                    'ErrorRate'
                            ]*/



            }
        }

        stage('Mark artifact for staging namespace') {

            steps {

                withCredentials([usernamePassword(credentialsId: 'dockerHub', passwordVariable: 'TOKEN', usernameVariable: 'USER')]) {
                    sh "docker login --username=${USER} --password=${TOKEN}"
                    sh "docker tag ${TAG_DEV} ${TAG_STAGING}"
                    sh "docker push ${TAG_STAGING}"
                }

            }
        }
    }
    post {

        always {

               sh 'docker-compose -f $WORKSPACE/docker-compose.yml down'
                 sh 'docker-compose -f  ${DOCKER_COMPOSE_LG_FILE}  down'

                  cleanWs()
                sh 'docker volume prune'
        }

    }

}

