package org.devops


//构建类型
def Build(buildType,buildShell){
    def buildTools = ["mvn":"M2","ant":"ANT","gradle":"GRADLE","npm":"NPM"]
    
    
    println("当前选择的构建类型为 ${buildType}")
    buildHome= tool buildTools[buildType]
    
    if ("${buildType}" == "npm"){
        
        sh  """ 
            export NODE_HOME=${buildHome} 
            export PATH=\$NODE_HOME/bin:\$PATH 
            ${buildHome}/bin/${buildType} ${buildShell}"""
    } else {
        sh "${buildHome}/bin/${buildType}  ${buildShell}"
    }
}

def NpmBuild() {
    println("正在构建npm项目")
    buildHome = tool "NPM"
    sh """
    export NODE_HOME=${buildHome}
    export export PATH=\$NODE_HOME/bin:\$PATH 
    node -v
    npm -v
    npm conf set registry https://registry.npm.taobao.org
    npm install
    npm run build:h5
    """
}