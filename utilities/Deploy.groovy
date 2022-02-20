@groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript
import groovy.transform.*
import org.yaml.snakeyaml.Yaml

/*
 * This script deploys batch and cics with or without DB2 Tar'ed by package.groovy
 * it bind dbrms and newcopy cics pgm's
 */


// define script properties
@Field def deployUtils = loadScript(new File("DeployUtilities.groovy"))
@Field def bindUtils = loadScript(new File("BindUtilities.groovy"))
@Field def refreshUtils = loadScript(new File("RefreshProgramUtilities.groovy"))

Deploy (args)

def DeployFiles (String workDir, boolean verbose ) {
	def pdsMapping 	= null
	def confDir 	= null
	def SUBSYS 		= null
	def COLLID 		= null
	def OWNER 		= null
	def QUAL 		= null
	def maxRC 		= null
	
	def cicsplex 	= null
	def cmciport 	= null

	println "** Deploying files from  $workDir   ..."
	def parser = new Yaml()	
	//def deploy = parser.load(("$workDir/pdsMapping.yaml" as File).getText("UTF-8"))
	def deploy = parser.load(("$workDir/pdsMapping.yaml" as File).text)
		    
	deploy.each{ node, value ->
		if ( "deploy" == node ) {
			cicsplex = value.'cics.cicsplex'
			cmciport = value.'cics.cmciport' as String
			maxRC = value.'bind.maxrc'.toInteger()
			confDir = value.'bind.confdir'
			OWNER = value.'bind.user'
			COLLID = value.'bind.package'
			QUAL = value.'bind.qualifier'
			SUBSYS = value.'bind.subsys'
			pdsMapping = value.'pds.mapping'
			
			println "\n Verbose: pdsMapping cmciPort=$cmciport"
		}
	}

	
	def pdsMap = [:]
	pdsMapping.split("\n").each { line ->
		pdsKey = line.replaceAll(/\*\.(.*),.*/, "\$1").trim()
		pdsValue = line.replaceAll(/.*,(.*)/, "\$1").trim()
		pdsMap.put(pdsKey, pdsValue)
	}

	
	//deploy (copy loads and DBRM) to target LPAR
	def appYamlFile = deployUtils.deployApplicationPackage(workDir)
	
	
	// Bind DBRM	
	def dbrmPds = pdsMap[ 'DBRM' ]
	def rc = bindUtils.bindYamlPackage(appYamlFile, dbrmPds, workDir, confDir, SUBSYS, COLLID, OWNER, QUAL, maxRC, verbose)

	// Run NEWCOPY
	rc = Math.max ( rc, refreshUtils.refreshYamlPackage(appYamlFile, workDir, cicsplex, cmciport, verbose) )
	
	return rc

}

//Parsing the command line
def Deploy(String[] cliArgs)
{
	def cli = new CliBuilder(usage: "Deploy.groovy [options]", header: '', stopAtNonOption: false)
	cli.h(longOpt:'help', 'Prints this message')
	//cli.t(longOpt:'tarFile', args:1, required:true, 'The application archive file.')
	//cli.y(longOpt:'yamlFile', args:1, required:true, 'The full path of the pdsMapping.yaml file.')
	cli.w(longOpt:'workDir', args:1, required:true, 'Absolute path to the working directory')
	cli.v(longOpt:'verbose', 'Flag to turn on script trace')
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
	
	def rc = DeployFiles (opts.w , opts.v)
	
	if  ( rc != 0 ) {
		System.exit(1)
	}
	
}