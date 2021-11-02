pakcage org.devops


//获取POM中的坐标
def GetGav(){
   //上传制品
    def jarName = sh returnStdout: true, script: "cd target;ls *.jar"
    // jarName = sh returnStdout: true, script: "cd target;ls *.jar"
    env.jarName = jarName - "\n"
    
    def pom = readMavenPom file: 'pom.xml'
    env.pomVersion = "${pom.version}"
    env.pomArtifact = "${pom.artifactId}"
    env.pomPackaging = "${pom.packaging}"
    env.pomGroupId = "${pom.groupId}"
    
    println("${pomGroupId}-${pomArtifact}-${pomVersion}-${pomPackaging}")

    return ["${pomGroupId}","${pomArtifact}","${pomVersion}","${pomPackaging}"]
}


//Nexus plugin deploy
def NexusUpload(){
    //use nexus plugin
    nexusArtifactUploader artifacts: [[artifactId: "${pomArtifact}", 
                                        classifier: '', 
                                        file: "${filePath}", 
                                        type: "${pomPackaging}"]], 
                            credentialsId: 'NEXUS_PASS', 
                            groupId: "${pomGroupId}", 
                            nexusUrl: 'nexus.node1.com', 
                            nexusVersion: 'nexus3', 
                            protocol: 'http', 
                            repository: "${repoName}", 
                            version: "${pomVersion}"
}

//mvn deploy
def MavenUpload(){          
    def mvnHome = tool "M2"
    sh  """
    cd target/
    ${mvnHome}/bin/mvn deploy:deploy-file \
    -Dmaven.test.skip=true  \
    -DgroupId=${pomGroupId} \
    -DartifactId=${pomArtifact} \
    -Dversion=${pomVersion}  \
    -Dpackaging=${pomPackaging} \
    -DrepositoryId=maven-snapshots \
    -Durl=http://nexus.node1.com/repository/maven-snapshots \
    -Dfile=${jarName} 
    """
}

//制品晋级
def ArtifactUpdate(updateType,artifactUrl){

    //晋级策略
    if ("${updateType}" == "snapshot -> release"){
        println("snapshot -> release")

        //下载原始制品
        sh "  rm -fr updates && mkdir updates && cd updates && wget ${artifactUrl} && ls -l "

        //获取artifactID 
        
        artifactUrl = artifactUrl -  "http://nexus.node1.com/repository/maven-snapshots/"
        artifactUrl = artifactUrl.split("/").toList()
        
        println(artifactUrl.size())
        env.jarName = artifactUrl[-1] 
        env.pomVersion = artifactUrl[-2].replace("SNAPSHOT","RELEASE")
        env.pomArtifact = artifactUrl[-3]
        pomPackaging = artifactUrl[-1]
        pomPackaging = pomPackaging.split("\\.").toList()[-1]
        env.pomPackaging = pomPackaging[-1]
        env.pomGroupId = artifactUrl[0..-4].join(".")
        println("${pomGroupId}##${pomArtifact}##${pomVersion}##${pomPackaging}")
        env.newJarName = "${pomArtifact}-${pomVersion}.${pomPackaging}"
        
        //更改名称
        sh " cd updates && mv ${jarName} ${newJarName} "
        
        //上传制品
        env.repoName = "maven-release"
        env.filePath = "updates/${newJarName}"
        NexusUpload()
    }
}


def main(uploadType){
    GetGav()
    if ("${uploadType}" == "maven"){
        MavenUpload()
    } else if ("${uploadType}" == "nexus") {
        env.repoName = "maven-snapshots"
        env.filePath = "target/${jarName}"
        NexusUpload()
    }
}
