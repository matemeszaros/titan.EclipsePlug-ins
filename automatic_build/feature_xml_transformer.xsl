<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output indent="yes"/>


	<!-- parameters:
					feature_version
					plugin_version
					update_site_name
					update_site_url
	-->

	<!--                                                     -->    
<!--	<xsl:input type=name="version_param" select="'UNDEFINED'"/> -->

	<!--                                                     -->    
	<xsl:template match="node()|@*">
		<xsl:copy>
			<xsl:apply-templates select="node()|@*"/>
		</xsl:copy>
	</xsl:template>

	<!--                                                     -->    
	<xsl:template match="feature/@version">
		<xsl:attribute name="version">
			<xsl:value-of select="$feature_version"/>
		</xsl:attribute>
	</xsl:template>

	<xsl:template match="feature/url/update/@label">
		<xsl:attribute name="label">
			<xsl:value-of select="$update_site_name"/>
		</xsl:attribute>
	</xsl:template>

	<xsl:template match="feature/url/update/@url">
		<xsl:attribute name="url">
			<xsl:value-of select="$update_site_url"/>
		</xsl:attribute>
	</xsl:template>

	<xsl:template match="feature/plugin/@version">
		<xsl:attribute name="version">
			<xsl:value-of select="$plugin_version"/>
		</xsl:attribute>
	</xsl:template>

</xsl:stylesheet>
