@Library('deploy')
import deploy

def deployLib = new deploy()

node {
    def application = "pam-ontologi-indexer"

    def committer, committerEmail, changelog, pom, releaseVersion

    def mvnHome = tool "maven-3.3.9"
    def mvn = "${mvnHome}/bin/mvn"
    def repo = "navikt"
    def deployEnv = "t1"
    def namespace = "t1"
    def appConfig = "nais.yaml"
    def dockerRepo = "repo.adeo.no:5443"
    def zone = 'sbs'
    def deployPullRequests = false

    def color

    try {

        stage("checkout") {
            checkout scm
        }

        stage("initialize") {
            commitHashShort = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
            releaseVersion = "1.0.${env.BUILD_NUMBER}-${commitHashShort}"
            committer = sh(script: 'git log -1 --pretty=format:"%an (%ae)"', returnStdout: true).trim()
            committerEmail = sh(script: 'git log -1 --pretty=format:"%ae"', returnStdout: true).trim()
            changelog = sh(script: 'git log `git describe --tags --abbrev=0`..HEAD --oneline', returnStdout: true)
        }

        stage("verify maven versions") {
            sh 'echo "Verifying that no snapshot dependencies is being used."'
            sh 'grep module pom.xml | cut -d">" -f2 | cut -d"<" -f1 > snapshots.txt'
            sh 'echo "./" >> snapshots.txt'
            sh 'while read line;do if [ "$line" != "" ];then if [ `grep SNAPSHOT $line/pom.xml | wc -l` -gt 1 ];then echo "SNAPSHOT-dependencies found. See file $line/pom.xml.";exit 1;fi;fi;done < snapshots.txt'
        }


        stage("build and test backend") {
            sh "${mvn} clean install -Dspring.profiles.active=prod -Djava.io.tmpdir=/tmp/${application} -B -e"
            sh "docker build --no-cache -t ${dockerRepo}/${application}:${releaseVersion} ."

        }


        stage("publish") {
          withCredentials([usernamePassword(credentialsId: 'nexusUploader', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
              sh "docker login -u ${env.NEXUS_USERNAME} -p ${env.NEXUS_PASSWORD} ${dockerRepo} && docker push ${dockerRepo}/${application}:${releaseVersion}"
              sh "curl --user ${env.NEXUS_USERNAME}:${env.NEXUS_PASSWORD} --upload-file ${appConfig} https://repo.adeo.no/repository/raw/nais/${application}/${releaseVersion}/nais.yaml"
          }
        }

        stage("deploy to preprod") {
		    callback = "${env.BUILD_URL}input/Deploy/"

		    def deploy = deployLib.deployNaisApp(application, releaseVersion, deployEnv, zone, namespace, callback, committer, false).key

		    try {
		        timeout(time: 15, unit: 'MINUTES') {
		            input id: 'deploy', message: "Check status here:  https://jira.adeo.no/browse/${deploy}"
		        }
		    } catch (Exception e) {
		        throw new Exception("Deploy feilet :( \n Se https://jira.adeo.no/browse/" + deploy + " for detaljer", e)
		    }
        }

         stage("Tag") {
                withEnv(['HTTPS_PROXY=http://webproxy-utvikler.nav.no:8088']) {
                    withCredentials([string(credentialsId: 'navikt-ci-oauthtoken', variable: 'token')]) {
                        sh ("git tag -a ${releaseVersion} -m ${releaseVersion}")
                        sh ("git push https://${token}:x-oauth-basic@github.com/${repo}/${application}.git --tags")
                    }
                }
            }
//
//
//        color = '#BDFFC3'
//        GString message = ":heart_eyes_cat: Siste commit på ${application} bygd og deploya OK.\nSiste commit ${changelog}"
//        slackSend color: color, channel: '#pam_bygg', message: message, teamDomain: 'nav-it', tokenCredentialId: 'pam-slack'


    } catch (e) {
//        color = '#FF0004'
//        GString message = ":crying_cat_face: :crying_cat_face: :crying_cat_face: :crying_cat_face: :crying_cat_face: :crying_cat_face: Halp sad cat! \n Siste commit på ${application} gikk ikkje gjennom. Sjå logg for meir info ${env.BUILD_URL}\nLast commit ${changelog}"
//        slackSend color: color, channel: '#pam_bygg', message: message, teamDomain: 'nav-it', tokenCredentialId: 'pam-slack'
    }

}
