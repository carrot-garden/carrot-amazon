package org.fusionatlas.enumerators

import net.tqft.toolkit.Logging

object LaunchQueueWorker {
  
	def main(args : Array[String]) : Unit = {
	  
//		LaunchQueueWorker.ssh("scott", "e8k3cvbh", "login.math.berkeley.edu", JAVA_HOME = Some("/home/u2/grad/scott/jre1.6.0_07"))
//		LaunchQueueWorker.ssh(passwordSession("scott", "e8k3cvbh"))
//		LaunchQueueWorker.ssh(keySession("root", new java.io.File("/Users/scott/home/ec2-keys/default.pem"), "ec2-67-202-3-101.compute-1.amazonaws.com"))
		LaunchQueueWorker.ec2("0D4BTQXQJ7SAKKQHF982", "wQsXfibiPzfPFDZ84jWXIjNb9UfqnLh42+FHhqtp", 2)
	}
	
	
	import com.jcraft.jsch._
	
	private def disableHostKeyChecking(session: Session) {
		val config: java.util.Properties = new java.util.Properties(); 
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
	}

	def keySession(username: String, key: java.io.File, hostname: String, port: Int = 22): Session = {
		val jsch = new JSch()
		jsch.addIdentity(key.getAbsolutePath())
		val session = jsch.getSession(username, hostname, port)
		
		disableHostKeyChecking(session)
		
		session	
	}
	
	def passwordSession(username: String, password: String, hostname: String = "localhost", port: Int = 22): Session = {
		val jsch = new JSch()
		val session = jsch.getSession(username, hostname, port)
		
		disableHostKeyChecking(session)
		
		session.setPassword(password)

		session
	}

	object ssh extends Object with Logging {
		def apply(session: Session, JAVA_HOME: Option[String] = None) {
			println("Opening SSH session to " + session.getHost + "...")
			session.connect()
			
			def exec(command: String): String = {
				val channel: ChannelExec = session.openChannel("exec").asInstanceOf[ChannelExec];
		        channel.setCommand(command);
		        channel.setInputStream(null);
		        
		        channel.connect();
		
		        val result = scala.io.Source.fromInputStream(channel.getInputStream()).getLines.foldLeft("")(_  + _ + "\n")
		        
		        channel.disconnect();
		        
		        result
			}
			
			println("Downloading preparation script...")
			val script = "prepare.sh"
			exec("curl http://tqft.net/svn/fusionatlas/package/scala/deploy/" + script + " > prepare.sh")
			println("Setting executable bit...")
			exec("chmod u+x ./prepare.sh")
			println("Running preparation script...")
			exec("./prepare.sh " + JAVA_HOME.getOrElse("") + " >& /dev/null &")
			println("Execution script detached.")
			
	        session.disconnect();
			println("Closing SSH session...")
		}
	}

	case class InstanceRequest(number: Int)
	case class AmazonAccount(id: String, key: String)

	object ec2 extends Object with Logging {
		import com.xerox.amazonws.ec2._

		def apply(id: String, key: String, number: Int = 2) {
			apply(AmazonAccount(id, key), InstanceRequest(number))
		}
		def apply(account: AmazonAccount, instanceRequest: InstanceRequest) {
			launch(account, instanceRequest) map { 
				hostname => ssh(keySession("root", new java.io.File("/Users/scott/home/ec2-keys/default.pem"), hostname)) 
			}
		}
		
		import scala.collection.JavaConversions._
		
		def launch(account: AmazonAccount, instanceRequest: InstanceRequest): List[String] = {
			val EC2 = new Jec2(account.id, account.key);
			
			val conf = new LaunchConfiguration("ami-8b38d3e2", instanceRequest.number, instanceRequest.number)
			conf.setKeyName("default")
			
			println("Launching " + instanceRequest.number + " instances...")
			val instanceIDs: List[String] = EC2.runInstances(conf).getInstances.toList map { _.getInstanceId }
			def instances: List[ReservationDescription#Instance] = (EC2.describeInstances(instanceIDs).toList flatMap { _.getInstances })
			var lastPendingId = ""
			while(
				instances.find(! _.isRunning) match {
					case Some(instance) => {
						val pendingId = instance.getInstanceId
						if(pendingId == lastPendingId) {
							print(".")
						} else {
							println()
							print("Waiting on " + pendingId)
							lastPendingId = pendingId
						}
						true
					}
					case None => {
						println()
						false
					}
				}
			) {
				Thread.sleep(1000)
			}
			
			(for(instance <- instances) yield {
				println("Instance " + instance.getInstanceId + " has public DNS: " + instance.getDnsName)
				instance.getDnsName
			}).toList
		}
		
	}
}
