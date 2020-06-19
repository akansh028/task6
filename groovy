job("G1") {
	
	scm { github("https://github.com/akansh028/task6.git") }
	
	triggers { githubPush() }

	wrappers { preBuildCleanup() }

	steps {
		dockerBuildAndPublish {
			repositoryname('akansh028/apache-webserver-php')
			tag("latest")
			dockerHostURI('tcp://0.0.0.0:1234')
			registeryCredentials('akansh028')
			createFingerprints(false)
			skipDecorate(false)
			skipTagAsLatest(true)
		}
	}	
}

job("G2") {
	
	description ( "Job to run the slave nodes of the kubernetes" )
	
	triggers { upstream('G2' , 'SUCCESS') }

	command = """

	export len1=\$(ls -l /var/lib/jenkins/workspace/G1 | grep html | wc -l)
	if [ \$len1 -gt 0 ]
	then
		export len2=\$(sudo kubectl get deployments | grep web1 | wc -l)
		if [ \$len2 -gt 0 ]
		then
			sudo kubectl rollout restart deployment/web1
			sudo kubectl rollout status deployment/web1
  		else
			sudo kubectl create deployment web1 --image=akansh028/apache-webserver-php:latest
			sudo kubectl scale deployment web1 --replicas=3
			sudo kubectl expose deployment web1 --port 80 --type NodePort
		fi
	fi

	"""
	
	steps { shell(command) }
		
}

job("G3") {

	description("Testing pods and sending mail")
 
	triggers { upstream('G2', 'SUCCESS') }

	steps{
	
		shell('''if sudo kubectl get deploy web1
			then
			echo "send this pod to prod env"
			else
			echo "send this pod back to dev env"
			exit 1
			fi''')
		}
	
	publishers {
        extendedEmail {
            contentType('text/html')
            triggers {
                success {
		    attachBuildLog(true)
                    subject('Successfull Build')
                    content('The build is successfull and deployment is done')
                    recipientList(akansh.agarwal23@gmail.com)
                    }
		failure {
		    attachBuildLog(true)
                    subject('Failed Build')
                    content('The build is unsuccessfull and deployment is not done')
                    recipientList(akansh.agarwal23@gmail.com)
                    }
                }
            }
        }
}


buildPipelineView('Devops Task6') {
	filterBuildQueue(true)
	filterExecutors(false)
  	title('Devops Task6')
 	displayedBuilds(1)
  	selectedJob('G1')
  	alwaysAllowManualTrigger(false)
  	showPipelineParameters(true)
  	refreshFrequency(1)
}














