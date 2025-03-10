def call() {
	def ci = """
apiVersion: v1
kind: Pod
metadata:
  namespace: jenkins-slave
  labels:
    role: slave
spec:
  serviceAccountName: jenkins
  containers:
  - name: jnlp
    image: docker.io/mochen2020/devops:jenkins-slave-v4
    volumeMounts:
    - mountPath: "/var/run/docker.sock"
      name: "volume-0"
      readOnly: false 
    - mountPath: "/root/.docker"
      name: "docker-cache"
    workingDir: /tmp
    env:
    - name: "JENKINS_AGENT_WORKDIR"
      value: "/tmp"
    imagePullPolicy: Always
  // tolerations:
  // - effect: NoSchedule
  //   key: devops-spot
  //   operator: Equal
  // nodeSelector:
  //   app: devops
  volumes:
  - hostPath:
      path: "/var/run/docker.sock"
    name: "volume-0"
  - emptyDir:
      medium: ""
    name: "workspace-volume"      
  - name: docker-cache
    emptyDir: {}
"""
	return ci;
}
