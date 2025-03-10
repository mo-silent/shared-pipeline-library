#!groovy
package org.docker

def createWebDockerfile(String path, Map METADATA){
    def create_docker_file = libraryResource 'plaud_web/create_docker_file.sh'
    writeFile text: create_docker_file, file: "./create_docker_file.sh", encoding: "UTF-8"
    sh "bash ./create_docker_file.sh ${METADATA.repo_dir} ${path} ${METADATA.GROUP_NAME}"
}

def kanikoPush(String tags,String path){
    sh """
        set +x
        cd ${path}
        /kaniko/executor --context `pwd` --dockerfile `pwd`/Dockerfile --destination ${tags}
    """
}