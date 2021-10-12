node {
  stage('📁️ Running Docker Container') {
    	sh """
    	sudo docker run -p 5432:5432 --rm -d --name db arminc/clair-db:latest
    	#wait to db to come up 
    	sleep 15
    	sudo docker run -p 6060:6060 --link db:postgres --rm -d --name clair arminc/clair-local-scan:latest
    	sleep 1
    	echo "checking container status"
    	sudo docker ps
    	"""       
  }
  stage('🔍️ Configure Clair') {
    if (fileExists('clair-scanner')) {
      echo 'Clair is configure'
      } 
      else {
        sh"""
          wget https://github.com/arminc/clair-scanner/releases/download/v12/clair-scanner_linux_amd64
          mv clair-scanner_linux_amd64 clair-scanner && sudo chmod +x clair-scanner
          ls -la            
        """     
      }
  }
    stage('🔍️ Starting Scan') {
      //Assigning the docker ip to the DOCKER_GATEWAY
      sh'''
        DOCKER_GATEWAY=$(sudo docker network inspect bridge --format "{{range .IPAM.Config}}{{.Gateway}}{{end}}")
        sudo ./clair-scanner --ip="$DOCKER_GATEWAY"  -r report.json ubuntu:18.04 || exit 0
      '''
    }    
}
