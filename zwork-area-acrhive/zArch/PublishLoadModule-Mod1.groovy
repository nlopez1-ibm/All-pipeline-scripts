@groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript
import java.io.File
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import org.apache.http.entity.FileEntity
import com.ibm.dbb.build.*
import com.ibm.dbb.build.DBBConstants.CopyMode
import com.ibm.dbb.build.report.BuildReport
import com.ibm.dbb.build.report.records.DefaultRecordFactory


moved the publist to art-push.sh

/************************************************************************************
 * This script publishes the outputs generated from a Package.groovy to artifactory.
 *
 * NJL: Customized  for Garanti
 ************************************************************************************/

// Load the Tools.groovy utility script
def tools = loadScript(new File("$scriptDir/Tools.groovy"))

// Parse command line arguments and load build properties
def usage = "PublishLoadModule.groovy [options]"
def opts = tools.parseArgs(args, usage)

def properties = tools.loadProperties(opts)

// get the src code webpage
def giturl = " "
if (properties.url.contains("@") ) {
	def temp1 = (properties.url.split('@'))
	def temp2 = (temp1[1].split(':'))
	def temp3 = (temp2[1].split(".git"))
	giturl = "https://" + temp2[0] + "/" + temp3[0] +  "/commit/" + properties.buildHash
} else {
   giturl = properties.url + "/commit/" + properties.buildHash
}
println("Github url :" + giturl)

def startTime = new Date()

properties.startTime = startTime.format("yyyyMMdd.hhmmss.mmm")
def commitHash = properties.buildHash

// Load the artifactory properties
properties.load(new File("${properties.confDir}/artifactory.properties"))


def workDir = properties.workDir

//? def loadDatasets = properties.loadDatasets


// Append build report
//? def exportBuildReport = new File("$tempLoadDir/BuildReport.json")
//? exportBuildReport << buildReportFile.text


//Set up the artifactory information to publish the tar file
def url = properties.get('artifactory.url')
def apiKey = properties.get('artifactory.apiKey')
def repo = properties.get('artifactory.repo') as String
def remotePath = "${buildGroup}/${tarFile.name}"

//Call the ArtifactoryHelpers to publish the tar file
def artifactoryHelpers = loadScript(new File("$scriptDir/ArtifactoryCurlHelpers.groovy"))
def artifactoryPullableURL = artifactoryHelpers.publish(url, repo, apiKey, remotePath, tarFile)


println " Tar published..."