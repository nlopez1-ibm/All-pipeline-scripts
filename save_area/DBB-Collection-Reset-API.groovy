/* Util to reset collectiion use DBB API
 * Run under groovyz
 */
@groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript
import com.ibm.dbb.repository.*
import com.ibm.dbb.dependency.*
import com.ibm.dbb.build.*
import com.ibm.dbb.build.report.*
import com.ibm.dbb.build.html.*

def client = new RepositoryClient()
//client.setUrl("HTTPS://dbbdev.rtp.raleigh.ibm.com:9443/dbb/")
client.setUrl("HTTPS://9.77.144.69:9443/dbb/")

client.setUserId("ADMIN")
client.setPassword("ADMIN")
client.forceSSLTrusted(true)

collections =['poc-workspace/poc-app-master',
              'poc-workspace/poc-app-master-outputs' , 
              'DAT-Demo-Workspace/Mortgage-SA-DAT-develop', 
              'DAT-Demo-Workspace/Mortgage-SA-DAT-develop-outputs' ]
              
collections.each { collection ->

		println("** Reset option selected for $collection")
			client.deleteCollection(collection)					
			client.deleteBuildResults(collection)			
}                                                                