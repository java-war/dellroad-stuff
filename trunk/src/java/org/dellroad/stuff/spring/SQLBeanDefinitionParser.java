
/*
 * Copyright (C) 2011 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.stuff.spring;

import org.dellroad.stuff.schema.SQLDatabaseAction;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Parses <code>&lt;dellroad-stuff:sql&gt;</code> tags.
 */
class SQLBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String SPLIT_PATTERN_ATTRIBUTE = "split-pattern";
    private static final String RESOURCE_ATTRIBUTE = "resource";
    private static final String CHARSET_ATTRIBUTE = "charset";

    @Override
    protected Class<?> getBeanClass(Element element) {
        return SQLDatabaseAction.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        // Get "split-pattern" attribute
        Attr attr = element.getAttributeNodeNS(null, SPLIT_PATTERN_ATTRIBUTE);
        if (attr != null)
            builder.addPropertyValue("splitPattern", attr.getValue());

        // Get "resource" attribute or nested SQL
        attr = element.getAttributeNodeNS(null, RESOURCE_ATTRIBUTE);
        if (attr != null) {

            // Verify no nested content
            Node node = element.getFirstChild();
            if (node != null)
                this.bogus();

            // Create resource reader
            BeanDefinitionBuilder resourceBuilder = BeanDefinitionBuilder.genericBeanDefinition(ResourceReaderFactoryBean.class);
            resourceBuilder.addPropertyValue("resource", attr.getValue());

            // Apply "charset" if any
            attr = element.getAttributeNodeNS(null, CHARSET_ATTRIBUTE);
            if (attr != null)
                resourceBuilder.addPropertyValue("characterEncoding", attr.getValue());

            // Set resource reader as the factory bean for the SQL script
            builder.addPropertyValue("SQLScript", resourceBuilder.getBeanDefinition());
        } else {

            // Verify at least one child node exists
            Node node = element.getFirstChild();
            if (node == null)
                this.bogus();

            // Concatenate all child nodes, which must be text
            StringBuilder buf = new StringBuilder();
            while (node != null) {
                if (!(node instanceof Text))
                    this.bogus();
                buf.append(((Text)node).getData());
                node = node.getNextSibling();
            }

            // Configure SQL script
            builder.addPropertyValue("SQLScript", buf.toString());
        }
    }

    private void bogus() {
        throw new BeanDefinitionValidationException("<dellroad-stuff:sql> beans must have either a \"resource\" attribute"
          + " or nested SQL content (but not both)");
    }
}

