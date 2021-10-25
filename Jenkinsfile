#!groovy

//引用jenkins shareLibrary
@Library('jenkinslib') _

//实例化jenkins shareLibrary
//必须和sharelibrary中的目录结构一致
def tools = new org.devops.tools()
def runBuild = new org.devops.build()
def gitlab = new org.devops.gitlab()

//初始化变量
String buildType = "${env.buildType}"
String buildShell = "${env.buildShell}"
String branchName = "${env.branchName}"
String repoUrl = "${env.repoUrl}"

//取出当前job触发方式
def job_causes = currentBuild.getBuildCauses().shortDescription[0].toString()

//打印当前job触发方式(手动/webhook)
String job_msg = "Current build method is: " + job_causes
tools.PrintMes(job_msg, "green")


//判断是否webhook方式触发，且是gitlab的push动作，如果条件符合，取出分支名，并重新赋值给branchName
//webhook触发器需要配置并取出如下几个变量：webhook_branch,userName,project_id,commitSha
if ( "${job_causes}" == "Generic Cause" && "${runOpts}" == "Gitlab_Push")  {
    
    //分割gitlab post过来的ref字段，取出分支名并赋值
    branchName = webhook_branch.split("/")[2]

    //打印gitlab webhook传过来的分支名
    String branch_msg = "Current branch is: " + branchName
    tools.PrintMes(branch_msg, "green")

    //webhookch触发方式修改job描述
    currentBuild.description = "Trigger by user: ${userName},The branch: ${branchName}"

    //修改gitlab CI/CD状态为running
    gitlab.ChangeCommitStatus(project_id, commitSha, "running")
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
                        tools.PrintMes("Jenkins share library!!!!!!!!", 'green')
                    }
                
                }
            }
        }
    }
    
    post {

        success {
            script {
                tools.PrintMes("Build success!!!", "green")
                gitlab.ChangeCommitStatus(project_id, commitSha, "success")
            }
        }

        failure {
            script {
                tools.PrintMes("shit, that is a failed pipeline!!!!", "red")
                gitlab.ChangeCommitStatus(project_id, commitSha, "failed")
            }
        }

        aborted {
            script {
                tools.PrintMes("User aborted current job", "red")
                gitlab.ChangeCommitStatus(project_id, commitSha, "canceled")
            }
        }

    }
}
