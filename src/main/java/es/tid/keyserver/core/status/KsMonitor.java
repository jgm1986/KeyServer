/**
 * Copyright 2016.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.tid.keyserver.core.status;

import es.tid.keyserver.config.ConfigController;
import es.tid.keyserver.controllers.db.DataBase;
import es.tid.keyserver.core.lib.CheckObject;
import es.tid.keyserver.core.lib.LastVersionAvailable;
import es.tid.keyserver.https.HttpsServerController;
import es.tid.keyserver.https.certificate.HttpsCert;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.Timer;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.slf4j.LoggerFactory;

/**
 * This class analyze the status of the main services from the KeyServer tool.
 * @author <a href="mailto:jgm1986@hotmail.com">Javier Martinez Gusano</a>
 * @since 0.3.0
 */
public class KsMonitor implements CheckObject{
	
    /**
     * Logging object.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(KsMonitor.class);
    
    /**
     * KeyServer startup date.
     */
    private final Date startDate;
    
    /**
     * Current KeyServer version object.
     */
    private final String curVer;
    
    /**
     * KeyServer Project GitHub URL
     */
    private final String repoUrl;
    
    /**
     * REDIS Database Connection Object
     */
    private DataBase dataBaseObj;
    
    /**
     * Timer for KeyServer status refresh every second.
     */
    private final Timer t1;
    
    /**
     * Timer for KeyServer certificate and updates status every 12 hours.
     */
    private final Timer t2;
    
    /**
     * Last KeyServer version available object manager.
     */
    private LastVersionAvailable updates;
    
    /**
     * Data base flag status. True connected, false if not.
     */
    private boolean dbStatus;
    
    /**
     * Flag used to alert only once time about the Data base connection lost.
     */
    private boolean dBnotified;
    
    /**
     * HTTPS server initialization status flag.
     */
    private HttpsServerController httpsServer;
    
    /**
     * This is the controller object for the HTTPs certificate.
     */
    private HttpsCert certStatus;
    
    /**
     * Flag for check if the object is correctly initialized.
     */
    private boolean isInitializated = false;
    
    /**
     * Class constructor.
     * @param db Data base controller object.
     * @param httpsServer HTTPs server object.
     * @param sCert HTTPs certificate controller object.
     * @param softwareConfig KeyServer configuration object.
     * @since v0.3.0
     */
    public KsMonitor(DataBase db, HttpsServerController httpsServer, HttpsCert sCert, ConfigController softwareConfig){ 
        // Get de current date when the KeyServer has started.
        startDate = new Date();
        // Set external object to class fields
        dataBaseObj = db;
        this.curVer = softwareConfig.getVersion();
        this.repoUrl = softwareConfig.getProjectPublicUrl();
        this.httpsServer = httpsServer;
        this.certStatus = sCert;
        // KeyServer updates object controller
        String apiRepoUrl = softwareConfig.getGitHubReleaseUrl();
        updates =  new LastVersionAvailable(apiRepoUrl);
        // Timer 1: Checks the object status every second.
        t1 = new Timer(softwareConfig.getChkDbInterval(), new ActionListener() {
            /**
             * Check every second the following objects.
             * @param ae Action event object (not used)
             */
            @Override
            public void actionPerformed(ActionEvent ae) {
                Thread.currentThread().setName("THDBMon");
                // Redis Data Base status.
                dbStatus = dataBaseObj.isConnected();
                if(!dbStatus && !dBnotified){
                     dBnotified = true;
                    // Error level.
                    LOGGER.error("Connection lost with Redis Database. Trying to connect...");
                } else if(dbStatus && dBnotified){
                    dBnotified = false;
                    LOGGER.info("Connected to Redis database.");
                }
            }
        });
        // Timer 2: Checks KeyServer updates and certificate status. Value
        //          specified by the user in milliseconds.
        t2 = new Timer(softwareConfig.getChkUpdateInterval(), new ActionListener() {
            /**
             * Check every 12 hours the following objects.
             * @param ae Action event object (not used)
             */
            @Override
            public void actionPerformed(ActionEvent ae) {
                Thread.currentThread().setName("UpdatesCertExpDate");
                // Check the HTTPs certificate expiration date.
                if(!certStatus.isValid()){
                    // If the keyserver is not updated.
                    LOGGER.error("The HTTPs certificate has expired since: {}\n\t"
                            + "All incoming requests to the KeyServer will be rejected.",
                            certStatus.certExpirDate());
                }
                // Get last version available:
                updates.refreshRepoStatus();
                if(!updates.isUpdated(curVer)){
                    // If the keyserver is not updated.
                    LOGGER.warn("There are a new version of KeyServer tool: {} Please update!\n\t"
                            + "KeyServer GitHub project download URL: {}",
                            updates.getLastVersionAvailable(), repoUrl);
                }
            }
        });
        // Start timers.
        t1.start();
        t2.start();
        isInitializated = true;
        LOGGER.trace("The KeyServer Monitor object has been started.");
    }
    
    /**
     * This method stops all the timers inside this object.
     * 
     *     <p>Please use this method before close KeyServer.
     * @since v0.3.0
     */
    public void stop(){
        t1.stop();
        t2.stop();
        LOGGER.trace("The KeyServer Monitor object has been stopped.");
    }
    
    /**
     * Returns the status flag for the Redis Database connection.
     * @return True if the KeyServer is connected to the RedisDatabase, false 
     *     otherwise.
     * @since v0.3.0
     */
    public boolean isRedisConnectionAvailable(){
        return this.dbStatus;
    }
    
    /**
     * This method is used to verify if the HTTPs object has been initialized
     *     correctly.
     * @return String with the HTTPS server status.
     * @since v0.3.0
     */
    public String httpsServerStatus(){
        return this.httpsServer.getStatus();
    }
    
    /**
     * This method is used to get the Date object with the HTTPs server 
     *     certificate expiration date.
     * @return Date object with the certificate expiration date.
     * @since v0.3.0
     */
    public Date getHttpsCertificateExpDate(){
        return this.certStatus.certExpirDate();
    }
    
    /**
     * This method is used to get the number of days where the current HTTPs 
     *     certificate is valid since today.
     * @return Long number with the number of valid days for the current HTTPs 
     *     certificate.
     * @since v0.3.0
     */
    public long getHttpsCertificateRemainDays(){
        return this.certStatus.certRemainDays();
    }
    
    /**
     * This method is used to get the version of the current KeyServer instance.
     * @return String with the version label for the current instance of the 
     *     KeyServer.
     * @since v0.3.0
     */
    public String getCurrentKSVersion(){
        return this.curVer;
    }
    
    /**
     * This method is used to get the last version available of the KeyServer on
     *     the public repository.
     * @return String with the label of the last KeyServer version available.
     * @since v0.3.0
     */
    public String getLastKSVersionAvailable(){
        return this.updates.getLastVersionAvailable();
    }
    
    /**
     * This method returns the KeyServer GitHub project URL as String.
     * @return String with the KeyServer project URL.
     * @since v0.3.0
     */
    public String getKSProjectURL(){
        return this.repoUrl;
    }
    
    /**
     * This method is used to get the Date object when the current instance of
     *     this key server was launch.
     * @return Date object when the current instance of the KeyServer was 
     *     executed.
     * @since v0.3.0
     */
    public Date keyServerRunningSince(){
        return this.startDate;
    }
    
    /**
     * This method returns a Jetty statistics object.
     * @return Statistic Jetty object.
     * @since v0.4.0
     */
    public StatisticsHandler getStatistics(){
        return this.httpsServer.getStatistics();
    }

    /**
     * Object initialization status.
     * @return Returns true if the object is correctly initialized or false if 
     *     not.
     * @since v0.4.3
     */
    @Override
    public boolean isCorrectlyInitialized() {
        return isInitializated;
    }
}
