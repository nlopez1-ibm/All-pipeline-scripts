@groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript
import groovy.transform.*

// define script properties
@Field int timeout = 30000
@Field def yamlUtils = loadScript(new File("YamlUtilities.groovy"))

/**
 * This script does a CICS NEWCOPY using curl and the CICS CMCI services
 * you can pass the yaml file created by package.groovy or a single program name
 */

refresh(args)

def refreshProgram (String workDir, String cicsplex, String program, String cmciport, boolean verbose) {
	println "** Refreshing CICS program $program"
	def curlCmd = "curl -v --silent -X PUT -H 'Content-type: application/xml' -d '<request><action name=\"NEWCOPY\"/></request>' 'http://$cicsplex:$cmciport/CICSSystemManagement/CICSProgram/${cicsplex}?CRITERIA=(PROGRAM=${program})'"
	
	if ( verbose )
		println("*** Executing $curlCmd")
		
	def shellCmd = new File("$workDir/refresh.sh")
	shellCmd.write "#!/bin/sh\n"
	shellCmd.write curlCmd

	chMod 	= "chmod +x $workDir/refresh.sh"
	chModOut= chMod.execute().text	
	
	def cmd = "sh $workDir/refresh.sh"
	def logFile = new File("${workDir}/${program}_refresh.log")
	
	StringBuffer out = new StringBuffer()
	StringBuffer err = new StringBuffer()

	Process process = cmd.execute()
	process.consumeProcessOutput(out, err)

	process.waitForOrKill(timeout)

	def rc = process.exitValue()
	logFile.write ("$out\n\n")
	
	 
			
	if (rc != 0 || err) {
		println("** Error-1 refreshing program $program. See ${workDir}/${program}_refresh.log")
		logFile.write ("Error:\n")
		logFile.write ("Command: $curlCmd:\n")
		logFile.write ("$err\n\n")
		println "\n**! $err" 
	}

	if ( out.toString().indexOf("api_response1_alt=\"OK\"") == -1 ) {
		println("** Error-2 refreshing program $program. See ${workDir}/${program}_refresh.log")
		logFile.write ("Error:\n")
		logFile.write ("Command: $curlCmd:\n")
		logFile.write ("$err\n\n")
		rc = 1
		
	}
	
	return rc
}

def refreshYamlPackage(String yamlFile, String workDir, String cicspex, String cmciport, boolean verbose) {	
	def _rc = 0
	def deploymentUnitsBlock = { unit ->
		if ( ! unit.deployType.endsWith("LOAD") )
			return
		println "** Processing deploy unit: ${unit.originPDS}, type ${unit.type}, deploy type ${unit.deployType}"
		return true;
	}

	def resourcesBlock = { unit, member  ->
		def rc = refreshProgram (workDir, cicspex, member.name, cmciport, verbose)
		_rc = Math.max ( _rc, rc )
	}
	
	yamlUtils.parseDeploymentUnitsAndResources("$yamlFile", deploymentUnitsBlock, resourcesBlock)
	
	return _rc
}

//Parsing the command line
def refresh(String[] cliArgs)
{
	def cli = new CliBuilder(usage: "RefreshProgramUtilities.groovy [options]", header: '', stopAtNonOption: false)
	cli.h(longOpt:'help', 'Prints this message')
	cli.p(longOpt:'program', args:1, 'Program Name. Exclusive with --yamlFile')
	cli.y(longOpt:'yamlFile', args:1, 'The full path of the yaml file. Exclusive with --program')
	cli.c(longOpt:'cicspex', args:1, required:true, 'Cics Plex')
	cli.i(longOpt:'cmciport', args:1, required:true, 'Cics cmci port')
	cli.w(longOpt:'workDir', args:1, required:true, 'Absolute path to the working directory')
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
	
	if ( opts.p && opts.y) {
		println "options --yamlFile and --program are exclusive"
		cli.usage()
		System.exit(1)
	}
	
	if  ( ! opts.p && ! opts.y ) {
		println "one of the options --yamlFile or --program must be present"
		cli.usage()
		System.exit(1)
	}
	
	def rc = 0
	if ( opts.p ) {
		rc = refreshProgram (opts.w, opts.c, opts.p, opts.i, opts.v)
	} else {
		rc = refreshYamlPackage(opts.y, opts.w, opts.c, opts.i, opts.v)
	}
	
	if  ( rc != 0 ) {
		System.exit(1)
	}
}