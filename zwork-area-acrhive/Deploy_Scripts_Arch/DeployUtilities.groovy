@groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript
import com.ibm.dbb.build.*
import com.ibm.dbb.build.DBBConstants.CopyMode
import groovy.transform.*

// define script properties
@Field def yamlUtils = loadScript(new File("YamlUtilities.groovy"))

def props = parseInput(args)

deployApplicationPackage (props.workDir, props.tarFile, new File(props.pdsMapping).text)

def deployApplicationPackage (String workDir, String tarFile, String pdsMapping) {

	// Local variables
	def tempFolderName = "${workDir}/tempDeploy"
	new File(tempFolderName).mkdirs()

	println "** Expand  application archive file"
	out = new StringBuffer()
	err = new StringBuffer()
	def tempFolder = new File(tempFolderName)
	process = "tar -xvf ${tarFile}".execute(null, tempFolder)
	process.waitForProcessOutput(out, err)
	rc = process.exitValue();
	if(rc!=0){
		print "ERR:\n" + err
		print "OUT:\n" + out
		assert rc == 0, "Failed to expand application archive file"
	}

	println "** Parse the application definition file"

	pdsMap = [:]
	pdsMapping.split("\n").each { line ->
		pdsKey = line.replaceAll(/\*\.(.*),.*/, "\$1").trim()
		pdsValue = line.replaceAll(/.*,(.*)/, "\$1").trim()
		pdsMap.put(pdsKey, pdsValue)
	}

	println "** Dataset Mapping $pdsMap"

	def srcOptions = "cyl space(1,15) lrecl(80) dsorg(PO) recfm(F,B) dsntype(library) msg(1)"
	def loadOptions = "cyl space(1,15) dsorg(PO) recfm(U) blksize(32760) dsntype(library) msg(1)"

	def copy = new CopyToPDS()
	def copyModeMap = ["NCAL": CopyMode.LOAD, "LOAD": CopyMode.LOAD, "CICS_LOAD": CopyMode.LOAD, "IMS_LOAD": CopyMode.LOAD, "DBRM": CopyMode.BINARY]
	def targetDataset = null;

	def deploymentUnitsBlock = { unit ->
		println "** Processing deploy unit: ${unit.originPDS}, type ${unit.type}, deploy type ${unit.deployType}"
		def suffix = unit.originPDS.replaceAll(/.*\.([^.]*)/, "\$1")
		targetDataset = pdsMap[suffix]

                //NJL:  Above logic could be expanded to us the deployType as the pdsMap lookup
                // Future enhancement.  Leaving as-is
                // Adding exception for Garanti support of NCAL loadLib
                if (unit.deployType.endsWith("NCAL")) {
                         targetDataset = pdsMap["NCAL"]
                }

		if ( targetDataset == null )
			return false
		if ( unit.deployType.endsWith("LOAD") || unit.deployType.endsWith("NCAL")) {
			new CreatePDS().dataset(targetDataset).options(loadOptions).create()
		}
		else {
			new CreatePDS().dataset(targetDataset).options(srcOptions).create()
		}
		copy.setCopyMode(copyModeMap[suffix])
		copy.setDataset(targetDataset)
		return true;
	}
	
	def resourcesBlock = { unit, member  ->
		println "*** Copying member $member.name to dataset $targetDataset"
		copy.file(new File("$tempFolderName/${unit.folder}/${member.name}")).member("${member.name}").copy()
	}

	yamlUtils.parseDeploymentUnitsAndResources("$tempFolderName/app.yaml", deploymentUnitsBlock, resourcesBlock)
	
	return "${tempFolderName}/app.yaml"
}

//Parsing the command line
def parseInput(String[] cliArgs)
{
	def cli = new CliBuilder(usage: "DeployUtilities.groovy [options]", header: '', stopAtNonOption: false)
	cli.h(longOpt:'help', 'Prints this message')
	cli.w(longOpt:'workDir', args:1, required:true, 'Absolute path to the working directory')
	cli.t(longOpt:'tarFile', args:1, required:true, 'Absolute path to zar file')
	cli.m(longOpt:'pdsMapping', args:1, required:true, 'Absolute path to pds mapping file')
	def opts = cli.parse(cliArgs)
	
	// if opt parse fail exit.
	if (! opts) {
		System.exit(1)
	}
	
	if (opts.h)
	{
		cli.usage()
		System.exit(0)
	}
	
	def props = new Properties()
	props.workDir = opts.w
	props.tarFile = opts.t
	props.pdsMapping = opts.m
	return props
}