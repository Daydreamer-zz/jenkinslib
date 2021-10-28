package org.devops


//scan
def SonarScan(sonarServer,projectName,projectDesc,projectPath,branchName){
    
    withSonarQubeEnv(sonarServer){
        
        scannerHome = tool "SONAR"
        nodeHome = tool "NPM"
        //def projectDesc = currentBuild.description
        def sonarDate = sh  returnStdout: true, script: 'date  "+%Y-%m-%d %H:%M:%S"'
        sonarDate = sonarDate - "\n"
        
        sh """
        export PATH=${nodeHome}/bin:${PATH}
        ${scannerHome}/bin/sonar-scanner \
        -Dsonar.projectKey="${projectName}" \
        -Dsonar.projectName="${projectName}" \
        -Dsonar.projectVersion="${sonarDate}" \
        -Dsonar.ws.timeout=30 \
        -Dsonar.projectDescription="${projectDesc}" \
        -Dsonar.links.homepage=http://www.baidu.com \
        -Dsonar.sources="${projectPath}" \
        -Dsonar.sourceEncoding=UTF-8 \
        -Dsonar.java.binaries=target/classes \
        -Dsonar.java.test.binaries=target/test-classes \
        -Dsonar.java.surefire.report=target/surefire-reports  \
        -Dsonar.branch.name="${branchName}" 
        #-X
        """
    }
    
    //def qg = waitForQualityGate()
    //if (qg.status != 'OK') {
        //error "Pipeline aborted due to quality gate failure: ${qg.status}"
    //}
}
