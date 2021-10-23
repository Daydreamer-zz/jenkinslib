#!groovy

//引用jenkins shareLibrary
@Library('jenkinslib') _

//实例化jenkins shareLibrary
//必须和sharelibrary中的目录结构一致
def tools = new org.devops.tools()


pipeline {
    agent any
    
    options {
        timestamps()
        
    }
    
    //参数化构建过程，和直接在web页面配置效果相同
    parameters { 
        string(name: 'DEPLOY_ENV', defaultValue: 'staging', description: '') 
    }

    
    stages {
        
        stage("Get Code") {
            steps {
                timeout(time:5, unit:"MINUTES") {
                    script {
                        println("get the code from git server")
                    }
                }
            }
        }
        
        stage("Building") {
            steps{
                timeout(time:20, unit:"MINUTES") {
                    script {
                        println("I m building from the source code")
                        //tool 使用jenkins全局工具
                        // nodeHome  = tool "nodejs-10"
                        // sh "${nodeHome}/bin/node -v"
                        nodejs('NODE') {
                            sh "node -v"
                        }
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
                println("shit, that is a failed pipeline!!!!")
            }
        }
    }
}
