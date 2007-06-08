// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2007 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.core.model.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Point;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataTable;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public abstract class AbstractNode implements INode {

    private String componentName;

    List<? extends IElementParameter> elementParameters;

    private List<? extends IConnection> outgoingConnections = new ArrayList<IConnection>();

    private List<? extends IConnection> incomingConnections = new ArrayList<IConnection>();

    private List<IMetadataTable> metadataList;

    private String pluginFullName;

    private String uniqueName;

    private boolean activate;

    private boolean start;

    private boolean subProcessStart;

    private IProcess process;

    private IComponent component;

    private boolean readOnly;

    private IExternalNode externalNode;

    private Boolean hasConditionalOutputs = Boolean.FALSE;
    
    private Boolean isMultiplyingOutputs = Boolean.FALSE;

    private List<BlockCode> blocksCodeCountToClose;
    
    private boolean isThereLinkWithHash;
    
    private boolean isThereLinkWithMerge;
    
    private Map<INode, Integer> mergeInfo;

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public List<? extends IElementParameter> getElementParameters() {
        return elementParameters;
    }

    public void setElementParameters(List<? extends IElementParameter> elementParameters) {
        this.elementParameters = elementParameters;
    }

    public List<? extends IConnection> getIncomingConnections() {
        return incomingConnections;
    }

    public void setIncomingConnections(List<? extends IConnection> incomingConnections) {
        this.incomingConnections = incomingConnections;
    }

    public List<? extends IConnection> getOutgoingConnections() {
        return outgoingConnections;
    }

    public void setOutgoingConnections(List<? extends IConnection> outgoingConnections) {
        this.outgoingConnections = outgoingConnections;
    }

    public List<IMetadataTable> getMetadataList() {
        return metadataList;
    }

    public void setMetadataList(List<IMetadataTable> metadataList) {
        this.metadataList = metadataList;
    }

    public String getPluginFullName() {
        return pluginFullName;
    }

    public void setPluginFullName(String pluginFullName) {
        this.pluginFullName = pluginFullName;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        for (IElementParameter param : elementParameters) {
            if (param.getName().equals("UNIQUE_NAME")) { //$NON-NLS-1$
                param.setValue(uniqueName);
            }
        }
        this.uniqueName = uniqueName;
    }

    public boolean isActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public boolean isSubProcessStart() {
        return subProcessStart;
    }

    public void setSubProcessStart(boolean subProcessStart) {
        this.subProcessStart = subProcessStart;
    }

    public void setPerformanceData(String perfData) {
        // null
    }

    public void setTraceData(String traceData) {
        // null
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#getReturns()
     */
    public List<? extends INodeReturn> getReturns() {
        return new ArrayList<INodeReturn>();
    }

    public IProcess getProcess() {
        return process;
    }

    public void setProcess(IProcess process) {
        this.process = process;
    }

    public void setComponent(IComponent component) {
        this.component = component;
    }

    public IComponent getComponent() {
        return component;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Will return the first item of the subprocess. If "withCondition" is true, if there is links from type RunIf /
     * RunAfter / RunBefore, it will return the first element found. If "withCondition" is false, it will return the
     * first element with no active link from type Main/Ref/Iterate.<br>
     * <i><b>Note:</b></i> This function doesn't work if the node has several start points (will return a random
     * start node).
     * 
     * @param withCondition
     * @return Start Node found.
     */
    public AbstractNode getSubProcessStartNode(boolean withConditions) {
        if (!withConditions) {
            if ((getCurrentActiveLinksNbInput(EConnectionType.FLOW_MAIN) == 0)
            // && (getCurrentActiveLinksNbInput(EConnectionType.FLOW_REF) == 0)
                    && (getCurrentActiveLinksNbInput(EConnectionType.ITERATE) == 0)) {
                return this;
            }
        } else {
            int nb = 0;
            for (IConnection connection : getIncomingConnections()) {
                if (connection.isActivate()) {
                    nb++;
                }
            }
            if (nb == 0) {
                return this;
            }
        }
        IConnection connec;

        for (int j = 0; j < getIncomingConnections().size(); j++) {
            connec = (IConnection) getIncomingConnections().get(j);
            if (!connec.getLineStyle().equals(EConnectionType.FLOW_REF)) {
                return ((AbstractNode) connec.getSource()).getSubProcessStartNode(withConditions);
            }
        }
        return null;
    }

    private int getCurrentActiveLinksNbInput(EConnectionType type) {
        int nb = 0;
        for (IConnection connection : getIncomingConnections()) {
            if (connection.isActivate() && connection.getLineStyle().equals(type)) {
                nb++;
            }
        }
        return nb;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#hasConditionnalOutputs()
     */
    public Boolean hasConditionalOutputs() {
        return this.hasConditionalOutputs;
    }

    /**
     * Sets the hasConditionnalOutputs.
     * 
     * @param hasConditionalOutputs the hasConditionnalOutputs to set
     */
    public void setHasConditionalOutputs(boolean hasConditionalOutputs) {
        this.hasConditionalOutputs = hasConditionalOutputs;
    }
    
    /**
     * Getter for isMultiplyingOutputs.
     * @return the isMultiplyingOutputs
     */
    public Boolean isMultiplyingOutputs() {
        return isMultiplyingOutputs;
    }

    
    /**
     * Sets the isMultiplyingOutputs.
     * @param isMultiplyingOutputs the isMultiplyingOutputs to set
     */
    public void setIsMultiplyingOutputs(Boolean isMultiplyingOutputs) {
        this.isMultiplyingOutputs = isMultiplyingOutputs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(uniqueName + " - ");
        buff.append("inputs:(");
        for (int i = 0; i < incomingConnections.size(); i++) {
            buff.append(incomingConnections.get(i).getName());
            if (i < (incomingConnections.size() - 1)) {
                buff.append(",");
            }
        }
        buff.append(") ");
        buff.append("outputs:(");
        for (int i = 0; i < outgoingConnections.size(); i++) {
            buff.append(outgoingConnections.get(i).getName());
            if (i < (outgoingConnections.size() - 1)) {
                buff.append(",");
            }
        }
        buff.append(")");
        return buff.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#getLocation()
     */
    public Point getLocation() {
        return null;
    }

    public IExternalNode getExternalNode() {
        return externalNode;
    }

    public void setExternalNode(IExternalNode externalNode) {
        this.externalNode = externalNode;
    }

    /**
     * Getter for isThereLinkWithHash.
     * 
     * @return the isThereLinkWithHash
     */
    public boolean isThereLinkWithHash() {
        return isThereLinkWithHash;
    }

    /**
     * Sets the isThereLinkWithHash.
     * 
     * @param isThereLinkWithHash the isThereLinkWithHash to set
     */
    public void setThereLinkWithHash(boolean isThereLinkWithHash) {
        this.isThereLinkWithHash = isThereLinkWithHash;
    }

    public IElementParameter getElementParameter(String name) {
        for (IElementParameter elementParam : elementParameters) {
            if (elementParam.getName().equals(name)) {
                return elementParam;
            }
        }
        return null;
    }

    public List<? extends IConnection> getOutgoingSortedConnections() {
        return org.talend.core.model.utils.NodeUtil.getOutgoingSortedConnections(this);
    }

    public List<? extends IConnection> getMainOutgoingConnections() {
        return org.talend.core.model.utils.NodeUtil.getMainOutgoingConnections(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#getOutgoingConnections(org.talend.core.model.process.EConnectionType)
     */
    public List<? extends IConnection> getOutgoingConnections(EConnectionType connectionType) {
        return org.talend.core.model.utils.NodeUtil.getOutgoingConnections(this, connectionType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.process.INode#getOutgoingConnections(java.lang.String)
     */
    public List<? extends IConnection> getOutgoingConnections(String connectorName) {
        return org.talend.core.model.utils.NodeUtil.getOutgoingConnections(this, connectorName);
    }

    
    public List<BlockCode> getBlocksCodeToClose() {
        return this.blocksCodeCountToClose;
    }

    
    public void setBlocksCodeCountToClose(List<BlockCode> blockCodesToClose) {
        this.blocksCodeCountToClose = blockCodesToClose;
    }

    /* (non-Javadoc)
     * @see org.talend.core.model.process.INode#renameData(java.lang.String, java.lang.String)
     */
    public void renameData(String oldName, String newName) {
        if (oldName.equals(newName)) {
            return;
        }

        for (IElementParameter param : this.getElementParameters()) {
            if (param.getValue() instanceof String) { // for TEXT / MEMO etc..
                String value = (String) param.getValue();
                if (value.contains(oldName)) {
                    param.setValue(value.replaceAll(oldName, newName));
                }
            } else if (param.getValue() instanceof List) { // for TABLE
                List<Map<String, Object>> tableValues = (List<Map<String, Object>>) param.getValue();
                for (Map<String, Object> line : tableValues) {
                    for (String key : line.keySet()) {
                        Object cellValue = line.get(key);
                        if (cellValue instanceof String) { // cell is text so rename data if needed
                            String value = (String) cellValue;
                            if (value.contains(oldName)) {
                                line.put(key, value.replaceAll(oldName, newName));
                            }
                        }
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.talend.core.model.process.INode#useData(java.lang.String)
     */
    public boolean useData(String name) {
        for (IElementParameter param : this.getElementParameters()) {
            if (param.getValue() instanceof String) { // for TEXT / MEMO etc..
                String value = (String) param.getValue();
                if (value.contains(name)) {
                    return true;
                }
            } else if (param.getValue() instanceof List) { // for TABLE
                List<Map<String, Object>> tableValues = (List<Map<String, Object>>) param.getValue();
                for (Map<String, Object> line : tableValues) {
                    for (String key : line.keySet()) {
                        Object cellValue = line.get(key);
                        if (cellValue instanceof String) { // cell is text so test data
                            if (((String) cellValue).contains(name)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public Map<INode, Integer> getLinkedMergeInfo() {
        return mergeInfo;
    }
    
    public void setLinkedMergeInfo(Map<INode, Integer> mergeInfo) {
        this.mergeInfo = mergeInfo;
    }

    public boolean isThereLinkWithMerge() {
        return isThereLinkWithMerge;
    }
    
    public void setThereLinkWithMerge(boolean isThereLinkWithHash) {
        this.isThereLinkWithMerge = isThereLinkWithHash;
    }
    
}
