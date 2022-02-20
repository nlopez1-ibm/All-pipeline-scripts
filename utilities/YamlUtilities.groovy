@groovy.transform.BaseScript com.ibm.dbb.groovy.ScriptLoader baseScript
import org.yaml.snakeyaml.Yaml

def parseDeploymentUnitsAndResources(String yamlFile, deploymentUnitsBlock, resourcesBlock) {
	def parser = new Yaml()
	def application = parser.load((yamlFile as File).getText("UTF-8"))
	application.each{ node, value ->
		if ( "deploymentUnits" == node ) {
			value.each{ unit ->
				if ( ! deploymentUnitsBlock(unit) )
					return
				unit.resources.each{ member ->
					resourcesBlock(unit, member)
				}
			}
		}
	}
}