node {
    properties([parameters([string(defaultValue: 'ubuntu:18.04', description: 'Enter image name', name: 'image'), string(defaultValue: 'report', description: 'Enter report name', name: 'report')])])
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
    stage('🔍️ Finding Vulnerabilites') {
      //Assigning the docker ip to the DOCKER_GATEWAY
      sh'''
        DOCKER_GATEWAY=$(sudo docker network inspect bridge --format "{{range .IPAM.Config}}{{.Gateway}}{{end}}")
        sudo ./clair-scanner --ip="$DOCKER_GATEWAY"  -r ${report}.json ${image} || exit 0
      '''
      echo "Clearing container"
      sh "sudo docker stop db clair"   
    } 
    stage('Converting JSON into HTML ') {    
         sh"""
         cat <<EOF > JSON_TO_HTML.py
import json 
from json2html import *
with open('${report}.json') as f:
    d = json.load(f)
    #converting and assign json to scanOutput
    scanOutput = json2html.convert(json=d)
    htmlReportFile = "${report}.html"
    with open(htmlReportFile, 'w') as htmlfile:
        htmlfile.write(str(scanOutput))
        print("Json file is converted into html")
EOF
         """
         echo "Configure python"
         sh"""
            sudo apt-get install python3 -y && sudo apt-get install python3-pip -y
            sudo pip install json2html
            sudo python3 JSON_TO_HTML.py
          """
          sh"ls"
         
    }   
}
