#!groovy

//引用jenkins shareLibrary
@Library('jenkinslib') _

//实例化jenkins shareLibrary
//必须和sharelibrary中的目录结构一致
def tools = new org.devops.tools()
def runBuild = new org.devops.build()

//取出当前job触发方式
def causes = currentBuild.getBuildCauses().shortDescription[0].toString()

//初始化变量
String buildType = "${env.buildType}"
String buildShell = "${env.buildShell}"
String branchName = "${env.branchName}"
String repoUrl = "${env.repoUrl}"


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
                        tools.PrintMes("get the code from git server", "green")

                        tools.PrintMes(causes.toString(), "red")

                        //判断是否webhook方式触发，且是gitlab的push动作，取出分支名，并重新赋值给branchName
                        if ( "${causes}" == "Generic Cause" && "${runOpts}" == "Gitlab_Push")  {
                            branchName = webhook_branch.split("/")[2]
                            tools.PrintMes(branchName, "red")
                        }


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
                        tools.PrintMes("I m building from the source code", "green")

                        //tool 使用jenkins全局工具
                        // nodejs('NODE') {
                        //     sh "node -v"
                        //     sh "npm -v"
                        // }
                        
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
        failure {
            script {
                tools.PrintMes("shit, that is a failed pipeline!!!!", "red")
            }
        }
    }
}
