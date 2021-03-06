// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.test.utils.testproperties;

import static org.talend.daikon.properties.PropertyFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.presentation.Form;
import org.talend.test.utils.SchemaProperties;

/**
 * 
 * created by ycbai on 2016年3月15日 Detailled comment
 *
 */
public class TestProperties extends ComponentProperties {

    public Property userId = newProperty("userId").setRequired(true); //$NON-NLS-1$

    public SchemaProperties schema = new SchemaProperties("schema"); //$NON-NLS-1$

    public TestNestedProperties nestedProps = new TestNestedProperties("nestedProps"); //$NON-NLS-1$

    public TestProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN, "Test Component"); //$NON-NLS-1$
        form.addRow(userId);
        form.addRow(nestedProps.getForm(Form.MAIN));
    }

}
