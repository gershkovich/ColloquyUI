<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    exclude-result-prefixes="xs"
    version="1.0">
    <xsl:output method="text" encoding="utf-8" omit-xml-declaration="yes"/>
    <xsl:template match="/">
	<xsl:apply-templates select="//se"/>
    </xsl:template>
    <xsl:template match="se">
        <xsl:apply-templates select=".//w"/>
    </xsl:template>
    <xsl:template match="w">
	<xsl:choose>
                <xsl:when test="ana">
			<xsl:value-of select="ana[1]/@lex"/>
		</xsl:when>
                <xsl:otherwise>
			<xsl:value-of select="."/>
		</xsl:otherwise>
	</xsl:choose>
	<xsl:text> </xsl:text>
    </xsl:template>
</xsl:stylesheet>
