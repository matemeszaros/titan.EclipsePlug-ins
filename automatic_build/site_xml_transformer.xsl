<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes" encoding="UTF-8"/>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="site">
		<xsl:copy>
			<xsl:copy-of select="description"/>
				<feature url="features/TITAN_Designer_{$version}.jar" id="TITAN_Designer" version="{$version}">
					<category name="TITAN_on_Eclipse"/>
				</feature>
				<xsl:text>&#xa;</xsl:text>
				<feature url="features/TITAN_Executor_{$version}.jar" id="TITAN_Executor" version="{$version}">
					<category name="TITAN_on_Eclipse"/>
				</feature>
				<xsl:text>&#xa;</xsl:text>
				<feature url="features/TITAN_Log_Viewer_{$version}.jar" id="TITAN_Log_Viewer" version="{$version}">
					<category name="TITAN_on_Eclipse"/>
				</feature>
				<feature url="features/Titanium_{$version}.jar" id="Titanium" version="{$version}">
					<category name="TITAN_on_Eclipse"/>
				</feature>
				<feature url="features/Titan_external_dependencies_{$version}.jar" id="Titan_external_dependencies" version="{$version}">
				  <category name="TITAN_on_Eclipse"/>
				</feature>
				<feature url="features/Titanium_external_dependencies_{$version}.jar" id="Titanium_external_dependencies" version="{$version}">
				  <category name="TITAN_on_Eclipse"/>
				</feature>
			<xsl:copy-of select="*[name() != 'description']"/> 
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
