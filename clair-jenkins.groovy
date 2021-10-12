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
    stage('📁️ Downloading Clair') {
      // sh"""
      // wget https://github.com/arminc/clair-scanner/releases/download/v12/clair-scanner_linux_amd64
      //  mv clair-scanner_linux_amd64 clair-scanner && sudo chmod +x clair-scanner
      //  ls -la
      // pwd
      //  """
    }
    stage('📁Starting Scan') {
     sh "sudo ./clair-scanner --ip 172.17.0.1 -r report.json ubuntu:18.04 || exit 0 "
    }
    
}
