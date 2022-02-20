@groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript
import com.ibm.dbb.build.*
import com.ibm.dbb.repository.*
import com.ibm.dbb.dependency.*
import groovy.transform.*

// define script properties.
@Field def yamlUtils = loadScript(new File("YamlUtilities.groovy"))

/**
 * This script builds a DB2 application package for SQL programs in the application.
 */

bind(args)

def bindPackage(String file, String dbrmHLQ, String workDir, String confDir, String SUBSYS, String COLLID, String OWNER, String QUAL, boolean verbose) {
	// define local properties
	def dbrmPDS     = "${dbrmHLQ}"
	def clistPDS    = "${dbrmHLQ}.CLIST"
	def cmdscpDS    = "${dbrmHLQ}.ISPFGWY.EXEC"
	def member      = CopyToPDS.createMemberName(file)
	def logFile     = new File("${workDir}/${member}_bind.log")
	def srcOptions  = "cyl space(1,1) lrecl(80) dsorg(PO) recfm(F,B) dsntype(library) msg(1)"


	println("*** Binding $file")


	// create BIND CLIST if necessary
	def clist = new File("${workDir}/bind.clist")
	if (clist.exists()) {
		clist.delete()
	}
	
	clist << """PROC 6 SUBSYS COLLID MEMBER LIB OWNER QUAL
WRITE BIND.CLIST STARTED
   DSN SYSTEM(&SUBSYS)
   BIND PACKAGE(&COLLID)    +
        MEMBER(&MEMBER)     +
        LIBRARY('&LIB')     +
        OWNER(&OWNER)       +
        QUALIFIER(&QUAL)    +
        ACTION(REPLACE)     +
        ISOLATION(CS)
   END
WRITE "BIND.CLIST ENDED RC = &LASTCC"
EXIT CODE(&LASTCC)
"""

	// create CLIST PDS if necessary
	new CreatePDS().dataset(clistPDS).options(srcOptions).create()

	// copy CLIST to PDS
	if ( verbose )
		println("*** Copying ${workDir}/bind.clist to $clistPDS(BIND)")
	new CopyToPDS().file(clist).dataset(clistPDS).member("BIND").execute()

	// bind the build file
	if ( verbose )
		println("*** Executing CLIST to bind program $file")

	println("*** Executing CLIST to bind program $file")

	// define TSOExec to run the bind clist

//	def bind = new TSOExec().file(file)
//			.command("exec '$clistPDS(BIND)'")
//			.options("'${SUBSYS} ${COLLID} $member $dbrmPDS ${OWNER} ${QUAL}'")
//			.logFile(logFile)
//			.confDir(confDir)
//			.keepCommandScript(true)
  //

	def bind = new TSOExec().command("exec '$clistPDS(BIND)'")
			.options("'${SUBSYS} ${COLLID} $member $dbrmPDS ${OWNER} ${QUAL}'")
			.logFile(logFile)
			.confDir(confDir)
			.keepCommandScript(true)

	bind.dd(new DDStatement().name("CMDSCP").dsn(cmdscpDS).options("shr"))

	// execute the bind clist
	def rc = bind.execute()
	
	return [rc,"${workDir}/${member}_bind.log"]

}

def bindYamlPackage(String yamlFile, String dbrmHLQ, String workDir, String confDir, String SUBSYS, String COLLID, String OWNER, String QUAL, int maxRC, boolean verbose) {
	def _rc = 0
	def deploymentUnitsBlock = { unit ->
		if ( unit.deployType != "DBRM" )
			return
		println "** Processing deploy unit: ${unit.originPDS}, type ${unit.type}, deploy type ${unit.deployType}"
		return true;
	}

	def resourcesBlock = { unit, member  ->
		def (rc, logFile) = bindPackage("${member.name}", dbrmHLQ, workDir, confDir, SUBSYS, COLLID, OWNER, QUAL, verbose)
		if ( rc > maxRC ) {
			String errorMsg = "*! The bind return code ($rc) for $member.name exceeded the maximum return code allowed ($maxRC)\n** See: $logFile"
			println(errorMsg)
			_rc = Math.max ( _rc, rc )
		}
	}
	
	yamlUtils.parseDeploymentUnitsAndResources("$yamlFile", deploymentUnitsBlock, resourcesBlock)
	
	return _rc
}

//Parsing the command linen and bind
def bind(String[] cliArgs)
{
	def cli = new CliBuilder(usage: "BindUtilities.groovy [options]", header: '', stopAtNonOption: false)
	cli.f(longOpt:'file', args:1, 'The file name or member name.Exclusive with --yamlFile')
	cli.y(longOpt:'yamlFile', args:1, 'The full path of the yaml file. Exclusive with --file')
	cli.d(longOpt:'dbrmHLQ', args:1, required:true, 'DBRM partition data sets')	
	cli.w(longOpt:'workDir', args:1, required:true, 'Absolute path to the working directory')
	cli.c(longOpt:'confDir', args:1, required:true, 'Absolute path to runIspf.sh folder')
	
	cli.s(longOpt:'subSys', args:1, required:true, 'The name of the DB2 subsystem')
	cli.p(longOpt:'collId', args:1, required:true, 'Specify the DB2 collection (Package)')
	cli.o(longOpt:'owner', args:1, required:true, 'The owner of the package')
	cli.q(longOpt:'qual', args:1, required:true, 'The value of the implicit qualifier')
	cli.m(longOpt:'maxRc', args:1, 'The maximun return value')
	
	cli.v(longOpt:'verbose', 'Flag to turn on script trace')
	
	def opts = cli.parse(cliArgs)
	
	// if opt parse fail exit.
	if (! opts) {
		System.exit(1)
	}
	
	if ( opts.f && opts.y) {
		println "options --yamlFile and --file are exclusive"
		cli.usage()
		System.exit(1)
	}
	
	if  ( ! opts.f && ! opts.y ) {
		println "one of the options --yamlFile or --file must be present"
		cli.usage()
		System.exit(1)
	}
	
	if (opts.help)
	{
		cli.usage()
		System.exit(0)
	}
	
	def maxRC = opts.m ? opts.m.toInteger() : 0
	if ( opts.f ) {
		def (rc, logFile) = bindPackage(opts.f, opts.d, opts.w, opts.c, opts.s, opts.p, opts.o, opts.q, opts.v)
		if ( rc > maxRC ) {
			String errorMsg = "*! The bind return code ($rc) for $opts.f exceeded the maximum return code allowed ($maxRC)\n** See: $logFile"
			println(errorMsg)
			System.exit(1)
		}
	} else {
		def rc = bindYamlPackage(opts.y, opts.d, opts.w, opts.c, opts.s, opts.p, opts.o, opts.q, maxRC, opts.v)
		if ( rc > maxRC ) {
			System.exit(1)
		}
	}
}

