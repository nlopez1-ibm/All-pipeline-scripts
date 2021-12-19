@groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript
import java.io.File
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import org.apache.http.entity.FileEntity
import com.ibm.dbb.build.*
import com.ibm.dbb.build.DBBConstants.CopyMode
import com.ibm.dbb.build.report.BuildReport
import com.ibm.dbb.build.report.records.DefaultRecordFactory
import groovy.json.JsonSlurper

/*
 * Packages DBB output into a TAR & create an app.yaml
 * NJL - Cert 3/20/21 Works on CLI Mode like ??? TBD - JUST SAVE FOR NOW  
 */

def properties = parseInput(args)
def startTime = new Date()
properties.startTime = startTime.format("yyyy-MM-dd'T'hh:mm:ss.mmm")
def workDir = properties.workDir
def loadCount = 0


//Retrieve the build report and parse the outputs from the build report
def buildReportFile = new File("$workDir/BuildReport.json")
assert buildReportFile.exists(), "$buildReportFile does not exist"
println "Using DBB $workDir/BuildReport.json"

def jsonSlurper = new JsonSlurper()
def parsedReport = jsonSlurper.parseText(buildReportFile.getText("UTF-8"))
def outputUnitFragments = [:]

// For each load module, use CopyToHFS with respective CopyMode option to maintain SSI
def copy = new CopyToHFS()
def copyModeMap = ["COPYBOOK": CopyMode.TEXT, "DBRM": CopyMode.BINARY, "LOAD": CopyMode.LOAD, "CICS_LOAD": CopyMode.LOAD]


//Create a temporary directory on zFS to copy the load modules from data sets to
def tempLoadDir = new File("$workDir/tempLoadDir")
!tempLoadDir.exists() ?: tempLoadDir.deleteDir()
tempLoadDir.mkdirs()

for (record in parsedReport.records) {
	if (record.outputs != null) {
		for (output in record.outputs) {
			if (output.dataset != null && output.deployType != null) {
				if (output.deployType != null && record.file != null ) {
					// This file need to be deployed
					def (dataset, member) = output.dataset.split("\\(|\\)")
						def key = "${dataset}#${output.deployType}";
						if ( outputUnitFragments[key] == null )
							outputUnitFragments[key] = "";
						outputUnitFragments[key] +=
							"      - name:           $member\n" +
							"        # NOTES - This can be the unique id of a load module or hash of a text\n" +
							"        hash:      "+Integer.toString(member.hashCode()).replace("-", "")+"\n"+
							"        sourceLocation:\n" +
							"          <<:           *gitHubGenAppSource\n" +
							"          path:         ${record.file}\n" +
							"          commitID:     ${properties.buildHash}\n" +
							"        buildScriptLocation:\n" +
							"          <<:           *gitHubGenAppBuild\n\n"
						
						// Copy the member	
						datasetDir = new File("$tempLoadDir/$dataset")
						datasetDir.mkdirs()
					
						currentCopyMode = copyModeMap[dataset.replaceAll(/.*\.([^.]*)/, "\$1")]
						copy.setCopyMode(currentCopyMode)
						copy.setDataset(dataset)
				
						copy.member(member).file(new File("$datasetDir/$member")).copy()
							
						loadCount++
				}
			}
		}
	}
}

assert loadCount > 0, "There are no load modules to publish"

//Create the application definition file.
def appYamlWriter = new File("$tempLoadDir/app.yaml")

//Set up the artifactory information to publish the tar file
def versionLabel = "${properties.startTime}"  as String

def tarFile = new File("$workDir/${properties.name}-${properties.version}.tar")
def remotePath = "${properties.version}/${properties.gitSourceBranch}/${properties.buildNumber}/${tarFile.name}"

println "${properties.url}/$remotePath"


appYamlWriter.withWriter("UTF-8") { writer ->
	writer.writeLine("name: ${properties.name}")
	writer.writeLine("version: ${properties.version}")
	writer.writeLine("creationTimestamp: \"${versionLabel}\"")
	
	writer.writeLine("package: ${properties.url}/$remotePath")
	
	writer.writeLine("packageType: partial")
	
	writer.writeLine("sources:")
	writer.writeLine("  - id:                 &gitHubGenAppSource")
	writer.writeLine("      type:               git")
	writer.writeLine("      branch:             ${properties.gitSourceBranch}")
	writer.writeLine("      uri:                ${properties.gitSourceUrl}")
	
	writer.writeLine("  - id:                 &gitHubGenAppBuild")
	writer.writeLine("      type:               git")
	writer.writeLine("      branch:             ${properties.gitBuildBranch}")
	writer.writeLine("      uri:                ${properties.gitBuildUrl}")
		
	writer.writeLine("deploymentUnits:")
	outputUnitFragments.each { key , fragment ->
		def (dataset, deployType) = key.split("#")
		// FIXME : archive should be mandatory
		//def process = "tar -cvf ${dataset}.tar ${dataset}".execute(null, tempLoadDir)
		//int rc = process.waitFor()
		//assert rc == 0, "Failed to package load modules"
		//"rm -rf ${dataset}".execute(null, tempLoadDir)
		//rc = process.waitFor()
		//assert rc == 0, "Failed to package load modules"
		writer.writeLine (
				" - originPDS:          $dataset\n" +
				"   type:               PDSE\n" +
				"   deployType:         $deployType\n" +
				"   folder:             $dataset\n"+
				"   resources:")
		writer.writeLine(fragment)
	}
}

println "Number of load modules to publish: $loadCount"

//Package the load files just copied into a tar file using the build
//label as the name for the tar file.
def process = "tar -cvf $tarFile .".execute(null, tempLoadDir)
def rc = process.waitFor()

assert rc == 0, "Failed to package application"

/*
 *  Methods
 *
 */
def parseInput(String[] cliArgs){
	println("parse output")
 def cli = new CliBuilder(usage: "package.groovy [options]")
 cli.w(longOpt:'workDir', args:1, argName:'dir', 'Absolute path to the DBB build output directory')
 cli.s(longOpt:'gitSourceUrl', args:1, argName:'url','The git source repo url')
 cli.g(longOpt:'gitBuildUrl', args:1, argName:'url','The git groovy build repo url')
 cli.x(longOpt:'gitSourceBranch', args:1, argName:'url','The git source repo branch')
 cli.y(longOpt:'gitBuildBranch', args:1, argName:'url','The git groovy build repo branch')
 cli.b(longOpt:'buildHash', args:1, argName:'hash','The git hash')
 cli.n(longOpt:'buildNumber', args:1, argName:'int','The build number')
 cli.a(longOpt:'url', args:1, argName:'url','The artifactory root url')
 cli.h(longOpt:'help', 'Prints this message')
 def opts = cli.parse(cliArgs)
 if (opts.h) { // if help option used, print usage and exit
	  cli.usage()
	 System.exit(0)
 }

 def properties = new Properties()

 // load workDir from ./build.properties if it exists
 def buildProperties = new Properties()
 def scriptDir = new File(getClass().protectionDomain.codeSource.location.path).parent
 def buildPropFile = new File("$scriptDir/conf/build.properties")
println "**TRACE**  $scriptDir/conf/build.properties"


 if (buildPropFile.exists()){

	 buildPropFile.withInputStream {
			 buildProperties.load(it)
	 }
	 properties.name = buildProperties.name
	 properties.version = buildProperties.version
 }

 // set command line arguments
 if (opts.w) properties.workDir = opts.w
 if (opts.s) properties.gitSourceUrl = opts.s
 if (opts.g) properties.gitBuildUrl = opts.g
 if (opts.x) properties.gitSourceBranch = opts.x else properties.gitSourceBranch = "master"
 if (opts.y) properties.gitBuildBranch = opts.y else properties.gitBuildBranch = "master"
 if (opts.b) properties.buildHash = opts.b
 if (opts.a) properties.url = opts.a
 if (opts.n) properties.buildNumber = opts.n

 // validate required properties
 try {
	 assert properties.workDir: "Missing property build output directory"
	 assert properties.gitSourceUrl : "Missing gitSourceUrl arg"
	 assert properties.gitBuildUrl : "Missing gitBuildUrl arg"
	 assert properties.buildHash : "Missing buildHash arg"
	 assert properties.buildNumber : "Missing buildNumber arg"
	 assert properties.url: "Missing url arg"
 } catch (AssertionError e) {
	 cli.usage()
	 throw e
 }
 return properties
}

