#!groovy

//引用jenkins shareLibrary
@Library('jenkinslib') _

//实例化jenkins shareLibrary
//必须和sharelibrary中的目录结构一致
def tools = new org.devops.tools()
def runBuild = new org.devops.build()
def gitlab = new org.devops.gitlab()
def sonarqube = new org.devops.sonarqube()
def sonarapi = new org.devops.sonarapi()
def nexus = new org.devops.nexus()

//初始化变量
String buildType = "${env.buildType}"
String buildShell = "${env.buildShell}"
String branchName = "${env.branchName}"
String repoUrl = "${env.repoUrl}"

//取出当前job触发方式并打印
def job_causes = currentBuild.getBuildCauses().shortDescription[0].toString()
String job_msg = "Current build method is: " + job_causes
tools.PrintMes(job_msg, "green")


//判断是否webhook方式触发，且是gitlab的push动作，如果条件符合，取出分支名，并重新赋值给branchName
//webhook触发器需要配置并取出如下几个变量：ref,user_username,project_id,checkout_sha
if ( "${job_causes}" == "Generic Cause" && "${runOpts}" == "Gitlab_Push")  {
    
    //分割gitlab post过来的ref字段，取出分支名并赋值
    branchName = ref.split("/")[2]

    //打印gitlab webhook传过来的分支名
    String branch_msg = "Current branch is: " + branchName
    tools.PrintMes(branch_msg, "green")

    //webhookch触发方式修改job描述
    currentBuild.description = "Trigger by user: ${user_username}, The branch name is: ${branchName}"

    //修改gitlab CI/CD状态为running
    gitlab.ChangeCommitStatus(project_id, checkout_sha, "running")
}



pipeline {
    agent any
    
    options {
        timestamps()
        
    }
    
    //参数化构建过程，和直接在web页面配置效果相同
    //parameters { 
    //    string(name: 'DEPLOY_ENV', defaultValue: 'staging', description: '') 
    //}

    
    stages {
        
        stage("Get Code") {
            steps {
                timeout(time:5, unit:"MINUTES") {
                    script {

                        tools.PrintMes("Pulling Code from git server", "green")

                        checkout([
                            $class: 'GitSCM', 
                            branches: [[name: "${branchName}"]], 
                            extensions: [], 
                            userRemoteConfigs: [[credentialsId: "GITLAB_PASS", url: "${repoUrl}"]]])
                    }
                    
                }
            }
        }
        
        stage("Building") {
            steps{
                timeout(time:20, unit:"MINUTES") {
                    script {
                        //tools.PrintMes("I m building from the source code", "green")

                        String build_msg = "Curret buildType is: " + buildType
                        tools.PrintMes(build_msg, "green")
                        //使用封装的share library
                        runBuild.Build("${buildType}","${buildShell}")
                    }
                }
            }
        }
        
        stage("Code Scan") {
            
            steps{
                
                //等待用户交互式输入
                // input id: 'test', message: '是否继续', ok: '是，请继续', parameters: [choice(choices: ['a', 'b'], name: 'test1')], submitter: 'admin,user'

                timeout(time:30, unit:"MINUTES") {
                    script {
                        //调用shareLibrary中定义的方法
                        tools.PrintMes("Scaning the codes....", 'green')

                        sonarqube.SonarScan("sonarqubeServer", JOB_NAME, currentBuild.description, WORKSPACE, branchName)

                        def scanResult = sonarapi.GetProjectStatus(JOB_NAME, branchName)
                        if (scanResult.toString() == "ERROR") {
                            error " 代码质量阈错误！请及时修复！"
                        } else {
                            println(scanResult)
                        }
                    }
                
                }
            }
        }

        stage("Upload to maven"){
        steps {
            timeout(time:30, unit:"MINUTES") {
                script {
                    //调用nexus封装sharelibrary初始化pom配置信息为全局变量
                    nexus.GetGav()

                    // 调用nexus封装sharelibrary执行mvn deploy上传制品
                    // nexus.MavenUpload()

                    //调用nexusArtifactUploader插件上传制品
                    env.repoName = "maven-snapshots"
                    env.filePath = "target/${jarName}"
                    nexus.NexusUpload()
                }
            }
        }
    }
    }
    
    post {

        success {
            script {
                tools.PrintMes("Build success!!!", "green")

                //如果是gitlab webhook触发的任务，才会访问gitlab api更新状态，手动执行的不会更新状态
                if ( "${job_causes}" == "Generic Cause" && "${runOpts}" == "Gitlab_Push")  {
                    gitlab.ChangeCommitStatus(project_id, checkout_sha, "success")
                }
            }
        }

        failure {
            script {
                tools.PrintMes("shit, that is a failed pipeline!!!!", "red")
                
                if ( "${job_causes}" == "Generic Cause" && "${runOpts}" == "Gitlab_Push")  {
                    gitlab.ChangeCommitStatus(project_id, checkout_sha, "failed")
                }
            }
        }

        aborted {
            script {
                tools.PrintMes("User aborted current job", "red")
                if ( "${job_causes}" == "Generic Cause" && "${runOpts}" == "Gitlab_Push")  {
                    gitlab.ChangeCommitStatus(project_id, checkout_sha, "canceled")
                }
            }
        }

    }
}

