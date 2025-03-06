def call() {
	def ci = """
apiVersion: v1
kind: Pod
metadata:
  namespace: jenkins-slave
  labels:
    role: slave
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::329599658616:role/jenkins_slave
spec:
  serviceAccountName: jenkins
  containers:
  - name: jnlp
    image: docker.io/mochen2020/devops:jenkins-slave-v1
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
    - name: AWS_ROLE_ARN
      value: "arn:aws:iam::329599658616:role/jenkins_slave"
    - name: AWS_WEB_IDENTITY_TOKEN_FILE
      value: "/var/run/secrets/eks.amazonaws.com/serviceaccount/token"
    - name: AWS_ROLE_SESSION_NAME
      value: "jenkins-slave-pod"
    imagePullPolicy: Always
  tolerations:
  - effect: NoSchedule
    key: devops-spot
    operator: Equal
  nodeSelector:
    app: devops
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
