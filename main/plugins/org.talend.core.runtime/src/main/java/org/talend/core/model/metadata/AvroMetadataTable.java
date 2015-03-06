// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.model.metadata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.BaseFieldTypeBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.SchemaBuilder.FieldTypeBuilder;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.avro.compiler.specific.SpecificCompiler.FieldVisibility;
import org.apache.avro.generic.GenericData.StringType;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.process.IProcess2;

/**
 * Meta Data Table. Contains all the columns. <br/>
 * $Id: MetadataTable.java 46622 2010-08-11 10:04:57Z wliu $
 */
public class AvroMetadataTable extends MetadataTable {

    private final static String AVRO_TEMPLATE_DIR = "/org/talend/core/model/metadata/avro/specific/"; //$NON-NLS-1$

    private Schema schema = null;

    private String filePath;

    private String technicalProjectName;

    private String jobName;

    /**
     * This constructor will extract process data to get the filePath, the technicalProjectName and the jobName of the
     * current metadata
     * 
     * @param process the current process
     */
    public AvroMetadataTable(IProcess2 process) {
        this((String) process.getElementParameter("FILE_PATH").getValue(), (String) process.getElementParameter( //$NON-NLS-1$
                "PROJECT_TECHNICAL_NAME").getValue(), process.getProperty().getLabel(), process.getProperty().getVersion()); //$NON-NLS-1$

    }

    /**
     * This constructor will extract project data to get the filePath, the technicalProjectName and the jobName of the
     * current metadata
     * 
     * @param projectPath the path to the sources of the project
     * @param technicalProjectName the name of the project
     * @param jobName the name of the job without version
     * @param jobVersion the version of the job
     */
    public AvroMetadataTable(String projectPath, String technicalProjectName, String jobName, String jobVersion) {
        super();
        // First and last char are quotes.
        if (projectPath.startsWith("\"")) { //$NON-NLS-1$
            this.filePath = projectPath.substring(1, projectPath.length() - 1);
        } else {
            this.filePath = projectPath;
        }
        // Fix path
        this.filePath = this.filePath.substring(0, projectPath.lastIndexOf("/")) + "/.Java/src/"; //$NON-NLS-1$ //$NON-NLS-2$

        this.technicalProjectName = technicalProjectName.toLowerCase();

        this.jobName = jobName + "_" + jobVersion.replace(".", "_"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        this.jobName = this.jobName.toLowerCase();
    }

    /**
     * This constructor use already extracted variable of the current metadata. It will be used by the clone() method.
     * 
     * @param filePath the path to the generated sources of the project
     * @param technicalProjectName the name of the project
     * @param jobName the name of the job with version
     */
    public AvroMetadataTable(String filePath, String technicalProjectName, String jobName) {
        super();

        this.filePath = filePath;
        this.technicalProjectName = technicalProjectName;
        this.jobName = jobName;
    }

    /**
     * cloned without custom columns by default.
     */
    @Override
    public IMetadataTable clone() {
        return clone(false);
    }

    @Override
    public IMetadataTable clone(boolean withCustoms) {
        AvroMetadataTable clonedMetadata = null;
        try {
            clonedMetadata = new AvroMetadataTable(filePath, technicalProjectName, jobName);

            List<IMetadataColumn> clonedMetaColumns = new ArrayList<IMetadataColumn>();
            clonedMetadata.setListColumns(clonedMetaColumns);
            for (int i = 0; i < super.getListColumns().size(); i++) {
                clonedMetaColumns.add(super.getListColumns().get(i).clone(withCustoms));
            }
            List<IMetadataColumn> clonedMetaUnusedColumns = new ArrayList<IMetadataColumn>();
            clonedMetadata.setUnusedColumns(clonedMetaUnusedColumns);
            for (int i = 0; i < super.getListUnusedColumns().size(); i++) {
                clonedMetaColumns.add(super.getListUnusedColumns().get(i).clone(withCustoms));
            }
            clonedMetadata.setTableName(this.getTableName());
            clonedMetadata.setLabel(this.getLabel());
            clonedMetadata.setAdditionalProperties(new HashMap<String, String>(super.getAdditionalProperties()));
        } catch (Exception e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
        return clonedMetadata;
    }

    /**
     * Generate the avro file associated to the current MetadataTable.
     * 
     * @param connectionName The name of the current connection. This parameter will be use to generate the file name.
     */
    public void generateAvroFile(String connectionName) {
        schema = generateAvroSchema(connectionName);

        try {
            // Generate the java class from the schema
            SpecificCompiler compiler = new SpecificCompiler(schema);
            compiler.setTemplateDir(AVRO_TEMPLATE_DIR);
            compiler.setFieldVisibility(FieldVisibility.PUBLIC);

            // Allow String java class
            compiler.setStringType(StringType.String);
            // No source, since we just want to parse the input schema
            compiler.compileToDestination(null, new File(filePath));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 
     * Generate the avro schema associated to the current MetadataTable.
     * 
     * @param connectionName The name of the current connection. This parameter will be use to generate the file name.
     */
    private Schema generateAvroSchema(String connectionName) {
        // Initialize the file with global parameters
        FieldAssembler<Schema> fieldAssembler = SchemaBuilder.record(connectionName + "Struct") //$NON-NLS-1$
                .prop(connectionName, connectionName).namespace(technicalProjectName + "." + jobName) //$NON-NLS-1$
                .fields();

        // Generate a field for each column of the metadatatable
        for (IMetadataColumn column : super.getListColumns()) {
            // Set field name
            BaseFieldTypeBuilder<Schema> fieldTypeSchema = fieldAssembler.name(column.getLabel()).type();

            // Set Nullable
            if (column.isNullable()) {
                fieldTypeSchema = ((FieldTypeBuilder<Schema>) fieldTypeSchema).nullable();
            }

            // Set field type
            if ("id_Boolean".equals(column.getTalendType())) { //$NON-NLS-1$
                fieldTypeSchema.booleanType().noDefault();
            } else if ("id_Byte".equals(column.getTalendType())) { //$NON-NLS-1$
                // TODO No native type byte
                fieldTypeSchema.stringBuilder().prop("java-class", "java.lang.Byte").endString().noDefault(); //$NON-NLS-1$  //$NON-NLS-2$
            } else if ("id_byte[]".equals(column.getTalendType())) { //$NON-NLS-1$
                // TODO is a byteBuffer
                fieldTypeSchema.bytesType().noDefault();
            } else if ("id_Character".equals(column.getTalendType())) { //$NON-NLS-1$
                // TODO No native type char
                fieldTypeSchema.stringBuilder().prop("java-class", "java.lang.Character").endString().noDefault(); //$NON-NLS-1$  //$NON-NLS-2$
            } else if ("id_Date".equals(column.getTalendType())) { //$NON-NLS-1$
                fieldTypeSchema.stringBuilder().prop("java-class", "java.util.Date").endString().noDefault(); //$NON-NLS-1$  //$NON-NLS-2$
            } else if ("id_Double".equals(column.getTalendType())) { //$NON-NLS-1$
                fieldTypeSchema.doubleType().noDefault();
            } else if ("id_Float".equals(column.getTalendType())) { //$NON-NLS-1$
                fieldTypeSchema.floatType().noDefault();
            } else if ("id_BigDecimal".equals(column.getTalendType())) { //$NON-NLS-1$
                fieldTypeSchema.stringBuilder().prop("java-class", "java.math.BigDecimal").endString().noDefault(); //$NON-NLS-1$  //$NON-NLS-2$
            } else if ("id_Integer".equals(column.getTalendType())) { //$NON-NLS-1$
                fieldTypeSchema.intType().noDefault();
            } else if ("id_Long".equals(column.getTalendType())) { //$NON-NLS-1$
                fieldTypeSchema.longType().noDefault();
            } else if ("id_Short".equals(column.getTalendType())) { //$NON-NLS-1$
                // TODO No native type short
                fieldTypeSchema.stringBuilder().prop("java-class", "java.lang.Short").endString().noDefault(); //$NON-NLS-1$  //$NON-NLS-2$
            } else if ("id_String".equals(column.getTalendType())) { //$NON-NLS-1$
                fieldTypeSchema.stringBuilder().prop("java-class", "java.lang.String").endString().noDefault(); //$NON-NLS-1$  //$NON-NLS-2$
            } else if ("id_List".equals(column.getTalendType())) { //$NON-NLS-1$
                fieldTypeSchema.stringBuilder().prop("java-class", "java.util.List").endString().noDefault(); //$NON-NLS-1$  //$NON-NLS-2$
            } else if ("id_Document".equals(column.getTalendType())) { //$NON-NLS-1$
                fieldTypeSchema.stringBuilder().prop("java-class", "routines.system.Document").endString().noDefault(); //$NON-NLS-1$  //$NON-NLS-2$
            } else if ("id_Dynamic".equals(column.getTalendType())) { //$NON-NLS-1$
                fieldTypeSchema.stringBuilder().prop("java-class", "routines.system.Dynamic").endString().noDefault(); //$NON-NLS-1$  //$NON-NLS-2$
            } else { // Object
                fieldTypeSchema.stringBuilder().prop("java-class", "java.lang.Object").endString().noDefault(); //$NON-NLS-1$  //$NON-NLS-2$
            }
        }

        return fieldAssembler.endRecord();
    }

}
