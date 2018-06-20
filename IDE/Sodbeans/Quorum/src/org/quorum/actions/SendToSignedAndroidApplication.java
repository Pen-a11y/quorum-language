/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.quorum.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.quorum.android.AndroidSetup;
import org.quorum.android.RunAndroid;
import org.quorum.projects.QuorumProject;
import org.quorum.projects.QuorumProjectType;
import org.sodbeans.phonemic.OperatingSystem;

/**
 *
 * @author stefika
 */
public class SendToSignedAndroidApplication extends QuorumAction implements ActionListener{
    public SendToSignedAndroidApplication(QuorumProject project) {
        super(project);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
            QuorumRunner runner = new QuorumRunner();
            Thread thread = new Thread(runner);
            thread.start();
    }
    
    private class QuorumRunner implements Runnable {
        @Override
        public void run() {
            try {
                QuorumAction.ProcessCancel cancel = new QuorumAction.ProcessCancel();
                String taskName = project.getProjectDirectory().getName() + " (Android)";
                
                final ProgressHandle progress = ProgressHandleFactory.createHandle(taskName, cancel);
                cancel.progress = progress;
                progress.start();
                BuildInformation info = build();
                boolean success = info.success;
                if(!success) {
                    progress.finish();
                    return;
                }
                
                final QuorumProjectType type = project.getProjectType();
                boolean legos = false;
                if(type == QuorumProjectType.LEGO) {
                    //io.getOut().println("To run the robot, use the lego robot itself. The reason is because I cannot execute the robot from within the development environment. ");
                    //io.getOut().close();
                    progress.finish();
                    return;
                }
                
                //to do this, I need the
                //executable name and path
                // Compute the location of the project's root directory.
                File runDirectory = project.getRunDirectory();
                String runName = runDirectory.getName() + "/" + project.getExecutableName(info.request);
                
                /* NOTE: 
                
                    By default, the system leaves these blank. If they are the empty
                    string, assume that the user has not defined them and the default
                    should be used.
                */
                String androidPath = project.getAndroidPath();
                String androidKeystorePath = project.getAndroidKeystorePath();
                String androidKeystorePassword = project.getAndroidKeystorePassword();
                String androidKeyAlias = project.getAndroidKeyAlias();
                String androidKeyPassword = project.getAndroidKeyPassword();
                String androidAlternateJDK = project.getAndroidAlternateJDK();
                
                AndroidSetup setup = new AndroidSetup();
                InstalledFileLocator locator = InstalledFileLocator.getDefault();
                File androidLocation = locator.locate("modules/Android", "org.quorum", false);
                setup.copyAndRename(androidLocation.getAbsolutePath(), runDirectory.getAbsolutePath(), project.getExecutableName(info.request), "t");
                
                //get all the properties, in case they are there.
                
                RunAndroid droid = new RunAndroid(runDirectory.getAbsolutePath());
                
                if (androidPath != null && !androidPath.equals("")) {
                    droid.setAndroidSDKPath(androidPath);
                }
                if (androidKeystorePath != null && !androidKeystorePath.equals("")) {
                    droid.setKeyStorePath(androidKeystorePath);
                }
                if (androidKeystorePassword != null && !androidKeystorePassword.equals("")) {
                    droid.setKeyStorePassword(androidKeystorePassword);
                }
                if (androidKeyAlias != null && !androidKeyAlias.equals("")) {
                    droid.setKeyAlias(androidKeyAlias);
                }
                if (androidKeyPassword != null && !androidKeyPassword.equals("")) {
                    droid.setKeyPassword(androidKeyPassword);
                }
                //location of where robovm is
//            InstalledFileLocator locator = InstalledFileLocator.getDefault();
//            File robovm = locator.locate("modules/ext/quorum-robovm", "org.quorum", false);
//
//                    
//            //the command
//            String robovmCommand = robovm.getAbsolutePath() + File.separator + "bin" + File.separator + "robovm";
//            
//            File robovmFileExec = new File(robovmCommand);
//            if(robovmFileExec.exists()) {
//                robovmFileExec.setExecutable(true);
//            }
//            
//            File iosSimFileExec = new File(robovm.getAbsolutePath() + File.separator + "bin" + File.separator + "ios-sim");
//            if(iosSimFileExec.exists()) {
//                iosSimFileExec.setExecutable(true);
//            }
//
//            
//            // Spawn a new Java process that will run "Default.jar" from the project directory.
//            String java = System.getProperty("java.home");
//            java += File.separator + "bin" + File.separator + "java";
//            
//            Properties properties = project.getLookup().lookup(Properties.class);
//            //now get all of the paramters from the user interface
//            String resources = properties.getProperty(QuorumProject.QUORUM_MOBILE_ASSETS_FOLDER);
//
//            
//            //Fake Example: "mb922625-1b6d-489c-54c8-65d204cfdb7c"
//            String signing = properties.getProperty(QuorumProject.QUORUM_IPHONE_SIGNING_KEY);
//            
//            //Fake Example: "'iPhone Developer: bob.timelord@tardis.edu (LHDJI2A9GY)'"
//            String provisioning = properties.getProperty(QuorumProject.QUORUM_IPHONE_PROVISION); 
//            String outputLocation = runDirectory.getAbsolutePath();
//            
//            resources = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath() + File.separator + resources;
//            //signing = "'" + signing + "'";
//            
//            String newName = project.getExecutableName(info.request);
//            newName = newName.substring(0, newName.length() - 3);
//            newName = newName + "ipa";
//            
//            String runFullPath = runDirectory.getAbsolutePath() + "/" + project.getExecutableName(info.request);
//            ProcessBuilder builder = new ProcessBuilder(robovmCommand, "-os", "ios", 
//                "-libs", "libfreetype.a:libGameEngineCPlugins.a:libObjectAL.a",
//                "-classpath", "robovm-cocoatouch-1.8.0.jar:robovm-rt-1.8.0.jar:robovm-objc-1.8.0.jar",
//                "-weakframeworks", "OpenGLES:UIKit:QuartzCore:CoreGraphics:OpenAL:AudioToolbox:AVFoundation",
//                "-jar", runFullPath, 
//                "-resources", resources,
//                "-signidentity", signing,
//                "-provisioningprofile", provisioning,
//                "-d", outputLocation,
//                "-createipa"
//            );
//            builder.directory(robovmFileExec.getParentFile());

//            io.getOut().println("Compiling to Android. This may take a few minutes.");
//            // Start the process.
//            Process process;
//            try {
//                process = builder.start();
//                QuorumAction.QuorumProcessWatcher watch = new QuorumAction.QuorumProcessWatcher(process.getErrorStream());
//                OutputStream outputStream = process.getOutputStream();
//                watch.setStream(outputStream);
//                watch.start();
//                cancel.process = process;
//                cancel.watcher = watch;
//                process.waitFor();
//                watch.wasDestroyed = true;
//                watch.cancelled = true;
//                watch.flush();
//                process.destroy();
//                progress.finish();
//            } catch (IOException ex) {
//                Exceptions.printStackTrace(ex);
//            } catch (InterruptedException ex) {
//                Exceptions.printStackTrace(ex);
//            }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    @Override
    public boolean isEnabled() {
        //check if we are on a mac. If not, disable the action
        OperatingSystem os = OperatingSystem.getOS();
        if(os != OperatingSystem.LINUX) {
            return true;
        }
        
        return false;
    }
    
    @Override
    protected String getDisplayName() {
        return "Generate Android Application for Store";
    }
}
