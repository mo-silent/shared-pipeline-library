def call() {
	def ci = """
apiVersion: v1
kind: Pod
metadata:
  namespace: jenkins-slave
  labels:
    role: slave
spec:
  securityContext:
    fsGroup: 1000
  serviceAccountName: default
  tolerations:
  - effect: NoSchedule
    key: devops-spot
    operator: Equal
  nodeSelector:
    app: devops
  containers:
    - name: kaniko
      image: gcr.io/kaniko-project/executor:debug
      command:
        - sleep
      args:
        - 99d
  restartPolicy: Never

"""
    return ci;
}