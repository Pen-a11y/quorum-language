/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quorum.windows;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import quorum.Libraries.Containers.Blueprints.Iterator$Interface;
import quorum.Libraries.Language.Compile.CompilerErrorManager$Interface;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.quorum.windows//CompilerError//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "CompilerErrorTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "org.quorum.windows.CompilerErrorTopComponent")
@ActionReferences({
    @ActionReference(path = "Menu/Window", position = 950),
    @ActionReference(path = "Shortcuts", name = "D-9")
})

@TopComponent.OpenActionRegistration(
        displayName = "#CTL_CompilerErrorAction",
        preferredID = "CompilerErrorTopComponent"
)
@Messages({
    "CTL_CompilerErrorAction=Errors",
    "CTL_CompilerErrorTopComponent=Errors",
    "HINT_CompilerErrorTopComponent=This window presents compiler errors"
})
public final class CompilerErrorTopComponent extends TopComponent {

    public CompilerErrorTopComponent() {
        initComponents();
        setName(Bundle.CTL_CompilerErrorTopComponent());
        setToolTipText(Bundle.HINT_CompilerErrorTopComponent());
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        errorTable = new javax.swing.JTable();

        errorTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Error", "Line", "File"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(errorTable);
        if (errorTable.getColumnModel().getColumnCount() > 0) {
            errorTable.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(CompilerErrorTopComponent.class, "CompilerErrorTopComponent.errorTable.columnModel.title0")); // NOI18N
            errorTable.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(CompilerErrorTopComponent.class, "CompilerErrorTopComponent.errorTable.columnModel.title1")); // NOI18N
            errorTable.getColumnModel().getColumn(2).setHeaderValue(org.openide.util.NbBundle.getMessage(CompilerErrorTopComponent.class, "CompilerErrorTopComponent.errorTable.columnModel.title2")); // NOI18N
        }
        errorTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CompilerErrorTopComponent.class, "CompilerErrorTopComponent.errorTable.AccessibleContext.accessibleName")); // NOI18N
        errorTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompilerErrorTopComponent.class, "CompilerErrorTopComponent.errorTable.AccessibleContext.accessibleDescription")); // NOI18N
        errorTable.getAccessibleContext().setAccessibleParent(this);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CompilerErrorTopComponent.class, "CompilerErrorTopComponent.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CompilerErrorTopComponent.class, "CompilerErrorTopComponent.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable errorTable;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }
    
    public void clear() {
        DefaultTableModel model = (DefaultTableModel) errorTable.getModel();
        int rowCount = model.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            model.removeRow(i);
        }
    }
    
    public void resetErrors(CompilerErrorManager$Interface manager) {
        DefaultTableModel model = (DefaultTableModel) errorTable.getModel();
        clear();
        Iterator$Interface it = manager.GetIterator();
        while(it.HasNext()) {
            quorum.Libraries.Language.Compile.CompilerError$Interface error = (quorum.Libraries.Language.Compile.CompilerError$Interface) it.Next();
            model.addRow(new Object[] {error.GetErrorMessage(), error.GetLineNumber(), error.GetAbsolutePath()});
        }
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}