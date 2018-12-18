<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="#all"
    version="2.0">
    <xsl:output method="text" indent="no"/>
    <xsl:template match="topic">
        <xsl:value-of select="concat(@id, ',,')"/>
        <xsl:for-each select="word">
            <xsl:value-of select="."/>
            <xsl:choose>
                <xsl:when test="current() = ../word[last()]">
                <xsl:text></xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>,</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>