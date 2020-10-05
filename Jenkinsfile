pipeline {
  agent {
    kubernetes {
      yaml """
apiVersion: v1
kind: Pod
metadata:
  annotations:
    traffic.sidecar.istio.io/excludeOutboundIPRanges: "0.0.0.0/0"
  labels:
    some-label: some-label-value
spec:
  containers:
  - name: java-ant-container
    image: cloudbees/jnlp-slave-with-java-build-tools:3.0.0
    volumeMounts:
      - mountPath: /var/tmp
        name: test-volume
    command: 
    - cat
    tty: true
    resources:
      requests:
        memory: "3Gi"
        cpu: "3000m"
      limits:
        memory: "5Gi"
        cpu: "5000m"
  volumes:
  - name: test-volume
    hostPath:
      # directory location on host
      path: /var/tmp
"""
    }
  }
  stages {
    stage('Compile'){
        steps{
        container('java-ant-container'){
						withAnt(installation: 'Ant') {
							sh "ant clean compile"
						}
			
        }
        }
    }
    
  }
}
