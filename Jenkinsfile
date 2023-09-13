properties(
    [parameters([
    choice(
    choices: 'main\nmaster\nbackup\nsplit-multiple-modules\nUS1038612-PDFExporter-refactoring', 
    description: 'Please select branch to build', 
    name: 'branch')]), 
    pipelineTriggers([])
    ])

pipeline {

   agent {label 'Slave1_Win'}
   
   stages {
    stage('Git checkout') { // for display purposes
        steps {   
            git url: 'git@github.com:EBSCOIS/platform.shared.cms-import.git', branch: "$branch"
            }
    }
    
    stage('Build CMS import tools') {

        steps {
            script {
            sh 'bash mvnw clean package'
            }
        } 
    }
   
    stage ('Validate folder content') {

        steps {
        sh'''
        for folder in *; do
        if [ -d "$folder" ]; then
                ls -la $folder/target/*$folder*
            fi
        done
        '''
        }    
    }
   
 }
 
    post {
        always {
        archive '*/target/*.zip, cm-importer/target/cm-importer-1.0.0.jar'
        cleanWs()
    } 
  }

}